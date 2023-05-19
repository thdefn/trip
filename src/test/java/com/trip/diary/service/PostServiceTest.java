package com.trip.diary.service;

import com.trip.diary.domain.model.*;
import com.trip.diary.domain.repository.LocationRepository;
import com.trip.diary.domain.repository.PostImageRepository;
import com.trip.diary.domain.repository.PostRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.domain.type.ParticipantType;
import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.PostDetailDto;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import com.trip.diary.util.ImageManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ImageManager imageManager;

    @InjectMocks
    private PostService postService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

    Member participant1 = Member.builder()
            .id(2L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();
    Member participant2 = Member.builder()
            .id(3L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .title("임의의 타이틀")
            .isPrivate(true)
            .description("임의의 설명")
            .leader(member)
            .participants(List.of(
                    Participant.builder()
                            .member(member)
                            .type(ParticipantType.ACCEPTED)
                            .build(),
                    Participant.builder()
                            .member(participant1)
                            .type(ParticipantType.ACCEPTED)
                            .build(),
                    Participant.builder()
                            .member(participant2)
                            .type(ParticipantType.ACCEPTED)
                            .build()
            ))
            .build();

    List<MultipartFile> images = List.of(
            new MockMultipartFile("images", "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes()),
            new MockMultipartFile("images", "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes())
    );

    @Test
    @DisplayName("여행 기록 생성 성공")
    void create_success() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도 도착입니당")
                .location("제주공항")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("제주공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(locationRepository.findFirstByTripOrderByIdDesc(any()))
                .willReturn(Optional.of(location));
        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));
        //when
        PostDetailDto result = postService.create(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Collection<PostImage>> postImageCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(postImageRepository, times(1)).saveAll(postImageCaptor.capture());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals(2, result.getImagePaths().size());
        assertEquals("제주도 도착입니당", postCaptor.getValue().getContent());
        assertEquals("제주공항", postCaptor.getValue().getLocation().getName());
        assertEquals(2, postImageCaptor.getValue().size());
        assertEquals(1L, postImageCaptor.getValue().iterator().next().getPost().getId());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());
    }

    @Test
    @DisplayName("여행 기록 생성 성공 - 이전 로케이션이 없을때")
    void create_successWhenOptionalLocationIsEmpty() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도로 출발입니당")
                .location("김포공항")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(locationRepository.findFirstByTripOrderByIdDesc(any()))
                .willReturn(Optional.empty());
        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));
        //when
        postService.create(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals("김포공항", postCaptor.getValue().getLocation().getName());
        assertEquals("김포공항", locationCaptor.getValue().getName());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());
    }

    @Test
    @DisplayName("여행 기록 생성 성공 - 직전 로케이션의 이름이 현재 위치와 다를 때")
    void create_successWhenPreviousLocationNameIsDifferent() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("진짜맛있다")
                .location("아베베 베이커리")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(locationRepository.findFirstByTripOrderByIdDesc(any()))
                .willReturn(Optional.of(location));
        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));
        //when
        postService.create(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals("아베베 베이커리", postCaptor.getValue().getLocation().getName());
        assertEquals("아베베 베이커리", locationCaptor.getValue().getName());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());
    }

    @Test
    @DisplayName("여행 기록 생성 실패 - 해당 여행 기록장 없음")
    void create_failWhenNotFoundTrip() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도 도착입니당")
                .location("제주공항")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class, () -> postService.create(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 생성 실패 - 유저가 여행 기록장에 대한 참여자가 아님")
    void create_failWhenNotAuthorityWriteTrip() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도 도착입니당")
                .location("제주공항")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(participant1)
                .participants(List.of(
                        Participant.builder()
                                .member(participant1)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.ACCEPTED)
                                .build()
                ))
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        //when
        TripException exception = assertThrows(TripException.class, () -> postService.create(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_WRITE_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 수정 성공")
    void update_success() {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("제주도 도착입니당")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .location(location)
                        .member(member)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build()));

        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));

        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        //when
        PostDetailDto result = postService.update(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Collection<PostImage>> postImageCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(postImageRepository, times(1)).saveAll(postImageCaptor.capture());
        verify(postImageRepository, times(1)).deleteAllInBatch(any());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals(2, result.getImagePaths().size());
        assertEquals("제주도 도착입니당", postCaptor.getValue().getContent());
        assertEquals("김포공항", postCaptor.getValue().getLocation().getName());
        assertEquals(2, postImageCaptor.getValue().size());
        assertEquals(1L, postImageCaptor.getValue().iterator().next().getPost().getId());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());

    }

    @Test
    @DisplayName("여행 기록 수정 실패 - 해당 기록 없음")
    void update_failWhenNotFoundPost() {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("제주도 도착입니당")
                .build();

        given(postRepository.findById(any())).willReturn(Optional.empty());
        //when
        PostException exception = assertThrows(PostException.class, () -> postService.update(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 수정 실패 - 유저가 기록의 작성자가 아님")
    void update_failWhenNotPostOwner() {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("제주도 도착입니당")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .location(location)
                        .member(participant1)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build()));
        //when
        PostException exception = assertThrows(PostException.class, () -> postService.update(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_POST_OWNER, exception.getErrorCode());
    }
}