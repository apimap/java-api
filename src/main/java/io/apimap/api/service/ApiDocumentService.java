package io.apimap.api.service;

import io.apimap.api.configuration.ApimapConfiguration;
import io.apimap.api.repository.IRESTConverter;
import io.apimap.api.repository.interfaces.IApi;
import io.apimap.api.repository.interfaces.IDocument;
import io.apimap.api.repository.repository.IApiRepository;
import io.apimap.api.repository.repository.IMetadataRepository;
import io.apimap.api.service.context.ApiContext;
import io.apimap.api.service.response.ResponseBuilder;
import io.apimap.api.utils.RequestUtil;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class ApiDocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDocumentService.class);

    final protected IRESTConverter entityMapper;
    final protected IMetadataRepository metadataRepository;
    final protected IApiRepository apiRepository;
    final protected ApimapConfiguration apimapConfiguration;

    public ApiDocumentService(final IRESTConverter entityMapper,
                              final IMetadataRepository metadataRepository,
                              final IApiRepository apiRepository,
                              final ApimapConfiguration apimapConfiguration) {
        this.metadataRepository = metadataRepository;
        this.apiRepository = apiRepository;
        this.apimapConfiguration = apimapConfiguration;
        this.entityMapper = entityMapper;
    }


    /*
    README.md
     */
    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createReadme(final ServerRequest request) {
        return createDocument(request, IDocument.DocumentType.README);
    }

    @NotNull
    public Mono<ServerResponse> getReadme(final ServerRequest request) {
        return getDocument(request, IDocument.DocumentType.README);
    }

    @NotNull
    public Mono<ServerResponse> getFormattedReadme(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> metadataRepository.getDocument(((IApi) api).getId(), context.getApiVersion(), IDocument.DocumentType.README))
                .flatMap(document -> {
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().sanitizeUrls(true).escapeHtml(true).build();
                    return Mono.justOrEmpty(renderer.render(parser.parse(((IDocument) document).getBody())));
                })
                .flatMap(text -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .text((String) text, MediaType.TEXT_HTML)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteReadme(final ServerRequest request) {
        return deleteDocument(request,  IDocument.DocumentType.README);
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateReadme(final ServerRequest request) {
        return updateDocument(request, IDocument.DocumentType.README);
    }

    /*
    CHANGELOG.md
     */
    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> createChangelog(final ServerRequest request) {
        return createDocument(request, IDocument.DocumentType.CHANGELOG);
    }

    @NotNull
    public Mono<ServerResponse> getChangelog(final ServerRequest request) {
        return getDocument(request, IDocument.DocumentType.CHANGELOG);
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> deleteChangelog(final ServerRequest request) {
        return deleteDocument(request,  IDocument.DocumentType.CHANGELOG);
    }

    @NotNull
    public Mono<ServerResponse> getFormattedChangelog(final ServerRequest request) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> metadataRepository.getDocument(((IApi) api).getId(), context.getApiVersion(), IDocument.DocumentType.CHANGELOG))
                .flatMap(document -> {
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().sanitizeUrls(true).escapeHtml(true).build();
                    return Mono.justOrEmpty(renderer.render(parser.parse(((IDocument) document).getBody())));
                })
                .flatMap(text -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .text((String) text, MediaType.TEXT_HTML)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    @NotNull
    @PreAuthorize("@Authorizer.isValidApiAccessToken(#request)")
    public Mono<ServerResponse> updateChangelog(final ServerRequest request) {
        return updateDocument(request, IDocument.DocumentType.CHANGELOG);
    }

    /*
    Metadata Document
     */

    protected Mono<ServerResponse> createDocument(final ServerRequest request, final IDocument.DocumentType type) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return request
                .bodyToMono(ByteArrayResource.class)
                .doOnNext(bytes -> {
                    if( bytes.contentLength() > apimapConfiguration.getLimits().getMaximumMetadataDocumentSize()){
                        throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size is " + apimapConfiguration.getLimits().getMaximumMetadataDocumentSize() + " byte(s)");
                    }
                })
                .flatMap(bytes -> entityMapper.decodeMetadataDocument(context, bytes, type))
                .flatMap(document -> apiRepository
                        .get(context.getApiName())
                        .flatMap(api -> metadataRepository.addDocument(((IApi) api).getId(), context.getApiVersion(), document))
                )
                .flatMap(document -> entityMapper.encodeMetadataDocument(uri, (IDocument) document))
                .flatMap(content -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .text((String) content, MediaType.TEXT_MARKDOWN)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }

    protected Mono<ServerResponse> getDocument(final ServerRequest request, final IDocument.DocumentType type) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> metadataRepository.getDocument(((IApi) api).getId(), context.getApiVersion(), type))
                .flatMap(document -> entityMapper.encodeMetadataDocument(uri, (IDocument) document))
                .flatMap(content -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .text((String) content, MediaType.TEXT_MARKDOWN)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    protected Mono<ServerResponse> deleteDocument(final ServerRequest request, final IDocument.DocumentType type) {
        final long startTime = System.currentTimeMillis();

        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return apiRepository
                .get(context.getApiName())
                .flatMap(api -> metadataRepository.deleteDocument(((IApi) api).getId(), context.getApiVersion(), type))
                .filter(value -> (Boolean) value)
                .flatMap(result -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .noContent())
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    protected Mono<ServerResponse> updateDocument(final ServerRequest request, final IDocument.DocumentType type) {
        final long startTime = System.currentTimeMillis();

        final URI uri = request.uri();
        final ApiContext context = RequestUtil.apiContextFromRequest(request);

        return request
                .bodyToMono(ByteArrayResource.class)
                .doOnNext(bytes -> {
                    if( bytes.contentLength() > apimapConfiguration.getLimits().getMaximumMetadataDocumentSize()){
                        throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size is " + apimapConfiguration.getLimits().getMaximumMetadataDocumentSize() + " byte(s)");
                    }
                })
                .flatMap(document -> entityMapper.decodeMetadataDocument(context, document, type))
                .flatMap(document -> apiRepository
                        .get(context.getApiName())
                        .flatMap(api -> metadataRepository.updateDocument(((IApi) api).getId(), context.getApiVersion(), document))
                )
                .flatMap(document -> entityMapper.encodeMetadataDocument(uri, (IDocument) document))
                .flatMap(content -> ResponseBuilder
                        .builder(startTime, apimapConfiguration)
                        .text((String) content, MediaType.TEXT_MARKDOWN)
                )
                .switchIfEmpty(Mono.defer(() -> ServerResponse.badRequest().build()));
    }
}
