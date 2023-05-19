package com.trip.diary.event;

import com.trip.diary.event.dto.ImageDeleteEvent;
import com.trip.diary.util.ImageManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostEventHandler {
    private final ImageManager imageManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteImageInStorage(ImageDeleteEvent event){
        imageManager.deleteImages(event.getImagePaths());
    }
}
