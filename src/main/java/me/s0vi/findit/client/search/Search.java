package me.s0vi.findit.client.search;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Search {
    final int radius;
    @NotNull
    final Identifier itemId;
    @Nullable
    final NbtCompound exactNbt;

    Results results = null;


    public Search(int radius, @NotNull Identifier itemId, @Nullable NbtCompound exactNbt) {
        this.radius = radius;
        this.itemId = itemId;
        this.exactNbt = exactNbt;
    }


    //TODO: Make this start at the center
    //TODO: Maybe run on server and call back to client?
    public CompletableFuture<Results> findInventories(ClientWorld world, BlockPos startPos) {
        return CompletableFuture.supplyAsync(() -> {
            Set<BlockPos> invs = new HashSet<>();

            //traverse a cube of (r*2-1)^2, check if in range, then if it's an inv, then if it satisfies the conditions
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    for (int y = -radius; y < radius; y++) {
                        BlockPos currentPos = startPos.add(x, y, z);

                        if (squaredDist(startPos, currentPos) <= radius * radius) {
                            BlockEntity blockEntity = world.getBlockEntity(currentPos);

                            //java 16 pattern matching!! took them long enough :rolling-eyes:
                            if (blockEntity instanceof Inventory inv) {
                                if(exactNbt == null) {
                                    boolean success = inv.containsAny(Set.of(Registry.ITEM.get(itemId)));

                                    if (success)
                                        invs.add(currentPos);
                                } else {
                                    //more expensive search
                                    for (int i = 0; i < inv.size(); i++) {
                                        if (inv.getStack(i) != null) {
                                            if (inv.getStack(i).getNbt() == exactNbt) {
                                                invs.add(currentPos);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }

            Results results = new Results(this, invs);
            this.results = results;

            return results;
        });
    }

    public boolean hasResults() {
        return !(results == null);
    }

    private int squaredDist(BlockPos pos1, BlockPos pos2) {
        //x^2 + y^2 + z^2
        int x = (pos2.getX() - pos1.getX());
        int y = (pos2.getY() - pos1.getY());
        int z = (pos2.getZ() - pos1.getZ());

        return x*x + y*y + z*z;
    }

    public record Results(Search search,
                          Set<BlockPos> blockSet) {

//        public Set<BlockPos> getBlocks() {
//            return blockSet;
//        }
//
//        public Search searchOrigin() {
//            return search;
//        }
    }
}
