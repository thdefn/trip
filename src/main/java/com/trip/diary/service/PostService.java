package com.trip.diary.service;

import com.trip.diary.domain.model.*;
import com.trip.diary.domain.repository.LocationRepository;
import com.trip.diary.domain.repository.PostImageRepository;
import com.trip.diary.domain.repository.PostRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.PostDetailDto;
import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import com.trip.diary.util.ImageManager;
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
    private final ImageManager imageManager;
    private final static String IMAGE_DOMAIN = "post";

    @Transactional
    public PostDetailDto create(Long tripId, CreatePostForm form, List<MultipartFile> images, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        if (!isMemberTripParticipants(trip.getParticipants(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        Location location = getNewlyLocation(trip, form.getLocation());
        Post savedPost = postRepository.save(Post.of(form, location, trip, member));
        List<String> imagePaths = savePostImages(savedPost, images);
        updateLocationThumbnail(location, imagePaths.get(0));
        return PostDetailDto.of(savedPost, imagePaths);
    }

    private void updateLocationThumbnail(Location location, String imagePath) {
        location.setThumbnailPath(imagePath);
        locationRepository.save(location);
    }

    private List<String> savePostImages(Post post, List<MultipartFile> images) {
        List<String> imagePaths = imageManager.uploadImages(images, IMAGE_DOMAIN);
        postImageRepository.saveAll(imagePaths.stream()
                .map(path -> PostImage.of(path, post))
                .collect(Collectors.toList()));
        return imagePaths;
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
        return participants.stream()
                .anyMatch(participant -> participant.getMember().getId().equals(memberId));
    }

    @Transactional
    public PostDetailDto update(Long postId,
                                UpdatePostForm form, List<MultipartFile> images, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        if (!Objects.equals(post.getMember().getId(), member.getId())) {
            throw new PostException(NOT_POST_OWNER);
        }

        deleteOldPostImages(post);

        post.setContent(form.getContent());
        List<String> imagePaths = savePostImages(post, images);
        updateLocationThumbnail(post.getLocation(), imagePaths.get(0));
        return PostDetailDto.of(postRepository.save(post), imagePaths);
    }

    private void deleteOldPostImages(Post post) {
        imageManager.deleteImages(post.getImages().stream()
                .map(PostImage::getImagePath)
                .collect(Collectors.toList()));
        postImageRepository.deleteAllInBatch(post.getImages());
    }
}
