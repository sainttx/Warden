package io.ll.warden.storage;

import io.ll.warden.utils.proxy.Warden;

public class AsyncUpdate implements Runnable {

    private final String query;

    public AsyncUpdate(String query) {
        this.query = query;
        Warden.getPluginContainer().get().getServer().getScheduler()
                .runTaskAsynchronously(Warden.getPluginContainer().get(), this);
    }

    @Override
    public void run() {
        Warden.getStorage().get().doQuery(this.query);
    }
}
