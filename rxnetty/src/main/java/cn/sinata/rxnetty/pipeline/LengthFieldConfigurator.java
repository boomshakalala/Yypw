package cn.sinata.rxnetty.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.reactivex.netty.pipeline.PipelineConfigurator;

public class LengthFieldConfigurator implements PipelineConfigurator<ByteBuf,ByteBuf> {

    @Override
    public void configureNewPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast("encoder", new LengthFieldPrepender(4));
    }
}