package ru.otus.listener.homework;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.otus.listener.Listener;
import ru.otus.model.Message;
import ru.otus.model.ObjectForMessage;

public class HistoryListener implements Listener, HistoryReader {

    // Используем ConcurrentHashMap для потокобезопасности (на случай многопоточности)
    private final Map<Long, Message> history = new ConcurrentHashMap<>();

    @Override
    public void onUpdated(Message msg) {
        if (msg == null) {
            return;
        }
        // Сохраняем глубокую копию сообщения, чтобы избежать мутаций извне
        Message safeCopy = deepCopyMessage(msg);
        history.put(msg.getId(), safeCopy);
    }

    @Override
    public Optional<Message> findMessageById(long id) {
        return Optional.ofNullable(history.get(id));
    }

    private Message deepCopyMessage(Message original) {
        ObjectForMessage originalField13 = original.getField13();
        ObjectForMessage copiedField13 = null;

        if (originalField13 != null) {
            copiedField13 = new ObjectForMessage();
            var originalData = originalField13.getData();
            if (originalData != null) {
                // Глубокая копия списка через List.copyOf (immutable list)
                copiedField13.setData(List.copyOf(originalData));
            }
        }

        return original.toBuilder().field13(copiedField13).build();
    }
}
