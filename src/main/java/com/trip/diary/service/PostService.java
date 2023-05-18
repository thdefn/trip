package com.trip.diary.service;

import com.trip.diary.domain.model.*;
import com.trip.diary.domain.repository.LocationRepository;
import com.trip.diary.domain.repository.PostImageRepository;
import com.trip.diary.domain.repository.PostRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.CreatePostDto;
import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import com.trip.diary.util.ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.trip.diary.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PostService {
    private final LocationRepository locationRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final TripRepository tripRepository;
    private final ImageUploader imageUploader;
    private final static String IMAGE_DOMAIN = "post";

    @Transactional
    public CreatePostDto create(Long tripId, CreatePostForm form, List<MultipartFile> images, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        if (!isMemberTripParticipants(trip.getParticipants(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        Location location = getNewlyLocation(trip, form.getLocation());
        List<String> imagePaths = imageUploader.uploadImages(images, IMAGE_DOMAIN);

        location.setThumbnailPath(imagePaths.get(0));
        locationRepository.save(location);
        return CreatePostDto.of(
                savePostAndPostImages(form.getContent(), imagePaths, location, trip),
                imagePaths);
    }

    private Post savePostAndPostImages(String content, List<String> imagePaths, Location location, Trip trip) {
        Post savedPost = postRepository.save(Post.builder()
                .content(content)
                .location(location)
                .trip(trip).build());
        postImageRepository.saveAll(imagePaths.stream()
                .map(path -> PostImage.of(path, savedPost))
                .collect(Collectors.toList()));
        return savedPost;
    }

    private Location getNewlyLocation(Trip trip, String locationName) {
        Optional<Location> optionalLocation = locationRepository.findFirstByTripOrderByIdDesc(trip);

        if (optionalLocation.isEmpty() ||
                !Objects.equals(optionalLocation.get().getName(), locationName)) {
            return Location.builder()
                    .name(locationName)
                    .trip(trip)
                    .build();
        }
        return optionalLocation.get();
    }

    private boolean isMemberTripParticipants(List<Participant> participants, Long memberId) {
        return participants.stream().anyMatch(participant -> participant.getMember().getId().equals(memberId));
    }

    @Transactional
    public CreatePostDto update(Long postId,
                                UpdatePostForm form, List<MultipartFile> images, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        if (!Objects.equals(post.getMember().getId(), member.getId())) {
            throw new PostException(NOT_POST_OWNER);
        }

        postImageRepository.deleteAllInBatch(post.getImages());

        post.setContent(form.getContent());
        List<String> imagePaths = imageUploader.uploadImages(images, IMAGE_DOMAIN);
        postImageRepository.saveAll(imagePaths.stream()
                .map(path -> PostImage.of(path, post))
                .collect(Collectors.toList()));
        return CreatePostDto.of(postRepository.save(post), imagePaths);
    }
}
