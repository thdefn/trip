package com.trip.diary.service;

import com.trip.diary.domain.model.*;
import com.trip.diary.domain.repository.*;
import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.PostDetailDto;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.event.dto.ImageDeleteEvent;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.LocationException;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import com.trip.diary.util.ImageManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.trip.diary.domain.constants.ParticipantType.ACCEPTED;
import static com.trip.diary.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PostService {
    private final LocationRepository locationRepository;
    private final ParticipantRepository participantRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final TripRepository tripRepository;

    private final PostLikeRedisRepository postLikeRedisRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ImageManager imageManager;
    private final static String IMAGE_DOMAIN = "post";

    @Transactional
    public PostDetailDto create(Long tripId, CreatePostForm form, List<MultipartFile> images, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        validationMemberHaveWriteAuthority(trip, member);

        Location location = getNewlyLocation(trip, form.getLocation());
        Post savedPost = postRepository.save(Post.of(form, location, trip, member));
        List<String> imagePaths = savePostImages(savedPost, images);
        updateLocationThumbnail(location, imagePaths.get(0));
        return PostDetailDto.of(savedPost, imagePaths, member.getId());
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

    private List<String> savePostImages(Post post, List<MultipartFile> images) {
        List<String> imagePaths = imageManager.uploadImages(images, IMAGE_DOMAIN);
        postImageRepository.saveAll(imagePaths.stream()
                .map(path -> PostImage.of(path, post))
                .collect(Collectors.toList()));
        return imagePaths;
    }

    private void updateLocationThumbnail(Location location, String imagePath) {
        location.setThumbnailPath(imagePath);
        locationRepository.save(location);
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
        return PostDetailDto.of(postRepository.save(post), imagePaths,
                postLikeRedisRepository.countByPostId(postId),
                postLikeRedisRepository.existsByPostIdAndUserId(post.getId(), member.getId()),
                member.getId());
    }

    private void deleteOldPostImages(Post post) {
        postImageRepository.deleteAllInBatch(post.getImages());
        applicationEventPublisher.publishEvent(
                new ImageDeleteEvent(post.getImages().stream()
                        .map(PostImage::getImagePath)
                        .collect(Collectors.toList())));
    }

    @Transactional
    public void delete(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        if (!Objects.equals(post.getMember().getId(), member.getId())) {
            throw new PostException(NOT_POST_OWNER);
        }

        if (post.getLocation().getPosts().size() == 1) {
            locationRepository.delete(post.getLocation());
        }
        postRepository.delete(post);

        applicationEventPublisher.publishEvent(
                new ImageDeleteEvent(post.getImages().stream()
                        .map(PostImage::getImagePath)
                        .collect(Collectors.toList())));
    }

    @Transactional
    public List<PostDetailDto> readPostsByLocation(Long locationId, Member member) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationException(ErrorCode.NOT_FOUND_LOCATION));

        validationMemberHaveReadAuthority(location.getTrip(), member);

        return location.getPosts().stream()
                .map(post -> PostDetailDto.of(post,
                        postLikeRedisRepository.countByPostId(post.getId()),
                        postLikeRedisRepository.existsByPostIdAndUserId(post.getId(), member.getId()),
                        member.getId()))
                .collect(Collectors.toList());
    }

    private void validationMemberHaveReadAuthority(Trip trip, Member member) {
        if (trip.isPrivate() && !participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED)) {
            throw new TripException(NOT_AUTHORITY_READ_TRIP);
        }
    }

    @Transactional
    public void like(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        validationMemberHaveWriteAuthority(post.getTrip(), member);

        if (postLikeRedisRepository.existsByPostIdAndUserId(postId, member.getId())) {
            postLikeRedisRepository.delete(postId, member.getId());
        } else {
            postLikeRedisRepository.save(postId, member.getId());
        }
    }

    private void validationMemberHaveWriteAuthority(Trip trip, Member member) {
        if (!participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED)) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }
    }
}
