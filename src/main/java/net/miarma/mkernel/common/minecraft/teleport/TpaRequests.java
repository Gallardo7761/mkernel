package net.miarma.mkernel.common.minecraft.teleport;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpaRequests {
    private List<TpaRequest> requests = new ArrayList<>();
    //private HashMap<Player,Long> cooldowns = new HashMap<>();
    private static TpaRequests instance;

    private TpaRequests() {}

    public static TpaRequests getInstance() {
        if(instance == null) {
            instance = new TpaRequests();
        }
        return instance;
    }

    public void addRequest(Player from, Player to, TpaType type) {
        TpaRequest existingRequest;
        if (type.equals(TpaType.TPA)) {
            existingRequest = getTpaRequest(from, to);
        } else {
            existingRequest = getTpaHereRequest(from, to);
        }

        /*if(!cooldowns.containsKey(from)) {
            cooldowns.put(from, System.currentTimeMillis());
        }*/

        if (existingRequest == null) {
            requests.addFirst(new TpaRequest(from, to, type));
        }
    }

    public TpaRequest getTpaRequest(Player from, Player to) {
        return requests.stream()
                .filter(request -> request.from().equals(from) && request.to().equals(to) && request.type().equals(TpaType.TPA))
                .findFirst()
                .orElse(null);
    }

    public TpaRequest getTpaHereRequest(Player from, Player to) {
        return requests.stream()
                .filter(request -> request.from().equals(from) && request.to().equals(to) && request.type().equals(TpaType.TPA_HERE))
                .findFirst()
                .orElse(null);
    }

    public List<TpaRequest> getRequests() {
        return requests;
    }

    /*public HashMap<Player,Long> getCooldowns() {
        return cooldowns;
    }

    public void removeCooldown(Player player) {
        cooldowns.remove(player);
    }

    public void getCooldown(Player player) {
        cooldowns.get(player);
    }*/

    public void removeRequest(TpaRequest request) {
        requests.remove(request);
    }

    public void clearRequests() {
        requests.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TpaRequest request : requests) {
            sb.append(request.from().getName())
                    .append(" -> ")
                    .append(request.to().getName())
                    .append(" (")
                    .append(request.type())
                    .append(")\n");
        }
        return sb.toString();
    }
}
