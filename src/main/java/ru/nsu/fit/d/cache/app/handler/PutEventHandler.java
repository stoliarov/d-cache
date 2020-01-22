package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.model.StoreValue;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.queue.event.Event;

public class PutEventHandler extends EventHandler {

    private SenderService senderService;

    public PutEventHandler() {
        this.senderService = new SenderService();
    }

    @Override
    public void handle(AppContext context, Event event) {

        if (isIrrelevantData(context, event)) {
            return;
        }

        String key = event.getKey();
        String value = event.getSerializedValue();
        long changeId = getNewChangeId(context);

        StoreValue storeValue = new StoreValue(value, changeId);

        context.getStore().put(key, storeValue);

        context.getStore().forEach((k, v) -> System.out.println(v.getSerializedValue()));

        senderService.sendConfirmation(context, event);

        if (!context.isWriter()) {
            return;
        }

        if (event.getFromHost() != null) {
            senderService.sendConfirmation(context, event);
        }

        System.out.println("Send broadcast (PUT)");

        senderService.initBroadcast(context, key, value, changeId);
    }

    private boolean isIrrelevantData(AppContext context, Event event) {

        return event.isLowPriorityValue() && context.getStore().containsKey(event.getKey());
    }

    private long getNewChangeId(AppContext context) {

        long newChangeId = context.getCurrentChangeId() + 1;
        context.setCurrentChangeId(newChangeId);

        return newChangeId;
    }
}
