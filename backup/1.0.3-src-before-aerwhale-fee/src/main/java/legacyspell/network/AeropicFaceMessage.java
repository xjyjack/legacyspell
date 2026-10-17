package legacyspell.network;

import io.netty.buffer.ByteBuf;
import legacyspell.LegacySpell;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AeropicFaceMessage implements IMessage {

    private float yaw;
    private float pitch;

    public AeropicFaceMessage() {
    }

    public AeropicFaceMessage(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        this.yaw = packet.readFloat();
        this.pitch = packet.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        packet.writeFloat(this.yaw);
        packet.writeFloat(this.pitch);
    }

    public static class Handler implements IMessageHandler<AeropicFaceMessage, IMessage> {

        @Override
        public IMessage onMessage(AeropicFaceMessage message, MessageContext context) {
            LegacySpell.proxy.handleAeropicFace(message.yaw, message.pitch);
            return null;
        }
    }
}
