package me.s0vi.findit.network;

import io.netty.util.ReferenceCountUtil;
import me.s0vi.findit.FindIt;
import me.s0vi.findit.client.FindItClient;
import me.s0vi.findit.search.Search;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class NetworkManager {
    public void initNetworkingServer() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.C2S_BEGIN_SEARCH_ID, (server, player, handler, buf, responseSender) -> {
            ReferenceCountUtil.retain(buf);
            server.execute(() -> {
                Search search = readSearchFromBuf(buf);
                FindItClient.LOGGER.info(ReferenceCountUtil.refCnt(buf));
                search.findInventories(player.getServerWorld(), player.getBlockPos(), player)
                        .exceptionally(e -> {
                            FindIt.LOGGER.error(e);
                            return null; //dunno why I need to return a value here, but whatever
                        });
            });
        });
    }

    public void initNetworkingClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.S2C_NEW_SEARCH_RESULT, (client, handler, buf, responseSender) -> {
            Search.Result r = readSearchResultFromBuf(buf);

            FindItClient.LOGGER.info("Received result: ");
            FindItClient.LOGGER.info("ID: {}", r.search().getUuid());
            FindItClient.LOGGER.info("Pos: {}", r.pos());

            FindItClient.INSTANCE.getSearchResultManager().submitResult(r.search().getUuid(), r);
        });
    }

    @Environment(EnvType.CLIENT)
    public void sendSearchToServer(Search search) {
        PacketByteBuf buf = writeSearchToBuf(search);
        ClientPlayNetworking.send(NetworkingConstants.C2S_BEGIN_SEARCH_ID, buf);
    }

    public void sendSearchResult(ServerPlayerEntity player, Search.Result result) {
        PacketByteBuf buf = writeSearchResultToBuf(result);

        ServerPlayNetworking.send(player, NetworkingConstants.S2C_NEW_SEARCH_RESULT, buf);
    }

    private PacketByteBuf writeSearchToBuf(Search search) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(search.getRadius());
        buf.writeIdentifier(search.getItemId());
        buf.writeNbt(search.getExactNbt());
        buf.writeUuid(search.getUuid());

        return buf;
    }

    private Search readSearchFromBuf(PacketByteBuf buf) {
        return new Search(
                buf.readInt(),
                buf.readIdentifier(),
                buf.readNbt(),
                buf.readUuid()
        );
    }

    private Search.Result readSearchResultFromBuf(PacketByteBuf buf) {
        return new Search.Result(
                readSearchFromBuf(buf),
                buf.readBlockPos()
        );
    }

    private PacketByteBuf writeSearchResultToBuf(Search.Result result) {
        PacketByteBuf buf = writeSearchToBuf(result.search());
        buf.writeBlockPos(result.pos());

        return buf;
    }
}
