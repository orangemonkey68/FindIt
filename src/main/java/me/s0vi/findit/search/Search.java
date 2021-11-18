package me.s0vi.findit.search;

import me.s0vi.findit.FindIt;
import me.s0vi.findit.Timer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Search {
    final int radius;
    @NotNull
    final Identifier itemId;
    @Nullable
    final NbtCompound exactNbt;
    final UUID uuid;

    public Search(int radius, Identifier itemId, @Nullable NbtCompound exactNbt) {
        this.radius = radius;
        this.itemId = itemId;
        this.exactNbt = exactNbt;
        uuid = UUID.randomUUID();
    }

    public Search(int radius, Identifier itemId, @Nullable NbtCompound exactNbt, UUID uuid) {
        this.radius = radius;
        this.itemId = itemId;
        this.exactNbt = exactNbt;
        this.uuid = uuid;
    }

    public int getRadius() {
        return radius;
    }

    public @NotNull Identifier getItemId() {
        return itemId;
    }

    @Nullable
    public NbtCompound getExactNbt() {
        return exactNbt;
    }

    public UUID getUuid() {
        return uuid;
    }

    private int squaredDist(BlockPos pos1, BlockPos pos2) {
        //x^2 + y^2 + z^2
        int x = (pos2.getX() - pos1.getX());
        int y = (pos2.getY() - pos1.getY());
        int z = (pos2.getZ() - pos1.getZ());

        return x*x + y*y + z*z;
    }

    public CompletableFuture<Void> findInventories(ServerWorld world, BlockPos startPos, ServerPlayerEntity playerEntity) {
        return CompletableFuture.runAsync(() -> {
            Timer timer = new Timer().start();

            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {
                    for (int z = -radius; z < radius; z++) {
                        BlockPos currentPos = startPos.add(x, y, z);

                        if (squaredDist(startPos, currentPos) <= radius * radius) {
                            CompletableFuture
                                    .supplyAsync(() -> world.getBlockEntity(currentPos), FindIt.SERVER)
                                    .thenAcceptAsync(blockEntity -> {
                                        if(blockEntity != null) {
                                            if (blockEntity instanceof Inventory inv) {
                                                if(exactNbt == null) {
                                                    boolean success = inv.containsAny(Set.of(Registry.ITEM.get(itemId)));

                                                    if (success)
                                                        FindIt.SERVER_NETWORK_MANAGER.sendSearchResult(playerEntity, new Result(this, currentPos));
                                                } else {
                                                    for (int i = 0; i < inv.size(); i++) {
                                                        ItemStack stack = inv.getStack(i);
                                                        if(stack != null && stack.getOrCreateNbt() == exactNbt) {
                                                            FindIt.SERVER_NETWORK_MANAGER.sendSearchResult(playerEntity, new Result(this, currentPos));
                                                        }
                                                    }
                                                }

                                                FindIt.LOGGER.info("Search took {} ms to complete.", timer.stop().getTimeMillis());
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }

//    public CompletableFuture<Void> findInventories(ServerWorld world, BlockPos startPos, ServerPlayerEntity player) {
//
//        //TODO: Refactor this to iterate over a stream of BlockPos's in parallel
//        return CompletableFuture.runAsync(() -> { //"hop" off main thread
//            Timer timer = new Timer().start();
//            FindIt.LOGGER.info(toString());
//
//            for (int x = -radius; x < radius; x++) {
//                for (int z = -radius; z < radius; z++) {
//                    for (int y = -radius; y < radius; y++) {
//                        BlockPos currentPos = startPos.add(x, y, z);
//
//                        if (squaredDist(startPos, currentPos) <= radius * radius) {
//                            CompletableFuture
//                                    .supplyAsync(() -> world.getBlockEntity(currentPos), FindIt.SERVER)
//                                    .thenAcceptAsync(blockEntity -> {
//                                        if(blockEntity != null) {
//                                            if(blockEntity instanceof Inventory inv) {
//                                                if(exactNbt == null) {
//                                                    boolean success = inv.containsAny(Set.of(Registry.ITEM.get(itemId)));
//
//                                                    for (int i = 0; i < inv.size(); i++) {
//                                                        FindIt.LOGGER.info(inv.getStack(i));
//                                                    }
//
//                                                    if (success)
//                                                        FindIt.LOGGER.info("Success at: {}", currentPos.toShortString());
//                                                    FindIt.SERVER_NETWORK_MANAGER.sendSearchResult(player, new Result(this, currentPos));
//                                                } else {
//                                                    //
//                                                    for (int i = 0; i < inv.size(); i++) {
//                                                        if (inv.getStack(i) != null) {
//                                                            if (inv.getStack(i).getNbt() == exactNbt) {
//                                                                FindIt.SERVER_NETWORK_MANAGER.sendSearchResult(player, new Result(this, currentPos));
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                            }
//                                    });
//                                }
//                            }
//                        }
//                    }
//                }
//
//            FindIt.LOGGER.info("Search took: {} ms", timer.stop().getTimeMillis());
//        }

    @Override
    public String toString() {
        return "Search{radius = %s, itemId = %s, exactNbt = %s, uuid = %s}".formatted(radius, itemId, exactNbt, uuid);
    }

    public record Result(Search search, BlockPos pos){}
}
