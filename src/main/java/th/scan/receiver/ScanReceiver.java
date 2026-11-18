package th.scan.receiver;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ScanReceiver implements ClientModInitializer {

    /** ข้อความจากปลั๊กอิน: int 4 ไบต์ = key code (GLFW) */
    public record KeyPayload(int key) implements CustomPayload {
        public static final CustomPayload.Id<KeyPayload> ID =
                new CustomPayload.Id<>(Identifier.of("scan", "key"));
        public static final PacketCodec<ByteBuf, KeyPayload> CODEC =
                PacketCodecs.INTEGER.xmap(KeyPayload::new, KeyPayload::key);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(KeyPayload.ID, KeyPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(KeyPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            int key = payload.key();
            client.execute(() -> {
                long window = client.getWindow().getHandle();
                client.keyboard.onKey(window, key, 0, GLFW.GLFW_PRESS, 0);
                client.keyboard.onKey(window, key, 0, GLFW.GLFW_RELEASE, 0);
            });
        });
    }
}
