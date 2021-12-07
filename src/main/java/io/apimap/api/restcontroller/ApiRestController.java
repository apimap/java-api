package io.apimap.api.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.apimap.api.repository.IApiRepository;
import io.apimap.api.repository.IClassificationRepository;
import io.apimap.api.repository.IMetadataRepository;
import io.apimap.api.repository.nitrite.entity.support.ApiCollection;
import io.apimap.api.repository.nitrite.entity.support.ClassificationCollection;
import io.apimap.api.repository.nitrite.entity.support.MetadataCollection;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.apimap.api.router.ApiRouter.ROOT_PATH;

/*
    This Rest Controller works alongside the Router methods to provide binary responses.
    Responses used create content backup collections.
 */
@RestController
public class ApiRestController {

    protected IApiRepository apiRepository;
    protected IMetadataRepository metadataRepository;
    protected IClassificationRepository classificationRepository;

    public ApiRestController(IApiRepository apiRepository,
                             IMetadataRepository metadataRepository,
                             IClassificationRepository classificationRepository) {
        this.apiRepository = apiRepository;
        this.classificationRepository = classificationRepository;
        this.metadataRepository = metadataRepository;
    }

    @RequestMapping(value=ROOT_PATH,
            produces="application/zip",
            method = RequestMethod.GET)
    public DataBuffer zipFiles() throws IOException {
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
        DataBuffer dataBuffer = nettyDataBufferFactory.allocateBuffer();
        ZipOutputStream zipOutputStream = new ZipOutputStream(dataBuffer.asOutputStream());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        ZipEntry apis = new ZipEntry("apis.json");

        zipOutputStream.putNextEntry(apis);
        ApiCollection apiCollection = apiRepository.all();
        zipOutputStream.write(mapper.writeValueAsBytes(apiCollection));

        ZipEntry classifications = new ZipEntry("classifications.json");
        zipOutputStream.putNextEntry(classifications);
        ClassificationCollection classificationCollection = classificationRepository.all();
        zipOutputStream.write(mapper.writeValueAsBytes(classificationCollection));

        ZipEntry metadata = new ZipEntry("metadata.json");
        zipOutputStream.putNextEntry(metadata);
        MetadataCollection metadataCollection = metadataRepository.all();
        zipOutputStream.write(mapper.writeValueAsBytes(metadataCollection));

        zipOutputStream.closeEntry();
        zipOutputStream.close();

        return dataBuffer;
    }
}
