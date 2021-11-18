package me.s0vi.findit.client.search;

import me.s0vi.findit.client.render.RenderManager;
import me.s0vi.findit.search.Search;

import java.util.*;

public class SearchResultManager {
    private final Map<UUID, Set<Search.Result>> resultMap = new HashMap<>();
    private final RenderManager renderManager;

    public SearchResultManager(RenderManager renderManager) {
        this.renderManager = renderManager;
    }

    public void submitResult(UUID searchUuid, Search.Result result) {
        resultMap.putIfAbsent(searchUuid, new HashSet<>());

        renderManager.addBlockToRender(searchUuid, result.pos());

        resultMap.get(searchUuid).add(result);
    }

    public Set<Search.Result> getResults(UUID searchUuid) {
        if(resultMap.containsKey(searchUuid)) {
            return resultMap.get(searchUuid);
        } else {
            throw new IllegalArgumentException("UUID \"%s\" does not exist.".formatted(searchUuid.toString()));
        }
    }
}
