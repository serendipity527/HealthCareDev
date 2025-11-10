package com.yihu.agent;

import ai.djl.util.Utils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.scoring.onnx.OnnxScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@SpringBootTest
@Slf4j
public class RAGTest {
    public static final String API_KEY = "sk-43070f4cd1074965a93a03d6d5333cd8";
    public static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    public static final String BASE_PATH = "C:\\Users\\v-lizhengfan\\IdeaProjects\\HealthCareDev\\src\\main\\resources\\documents";
    public final ChatModel CHAT_MODEL = OpenAiChatModel.builder()
            .apiKey(API_KEY)
            .baseUrl(BASE_URL)
            .modelName("qwen3-max")
            .build();
    public static final EmbeddingModel EMBEDDING_MODEL = new AllMiniLmL6V2EmbeddingModel();


    @Service
    interface Assistant {

        public String chat(String userMessage);


    }


    @Test
    public void easyRAG() {
        ChatModel CHAT_MODEL = OpenAiChatModel.builder()
                .apiKey(API_KEY)
                .baseUrl(BASE_URL)
                .modelName("qwen3-max")
                .build();
        // First, let's load documents that we want to use for RAG
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(BASE_PATH);

        // Second, let's create an assistant that will have access to our documents
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL) // it should use OpenAI LLM
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // it should remember 10 latest messages
                .contentRetriever(createContentRetriever(documents)) // it should have access to our documents
                .build();
        String res = assistant.chat("你好");
        System.out.println(res);
        // Lastly, let's start the conversation with the assistant. We can ask questions like:
        // - Can I cancel my reservation?
        // - I had an accident, should I pay extra?
    }

    private static ContentRetriever createContentRetriever(List<Document> documents) {

        // Here, we create an empty in-memory store for our documents and their embeddings.
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Here, we are ingesting our documents into the store.
        // Under the hood, a lot of "magic" is happening, but we can ignore it for now.
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        // Lastly, let's create a content retriever from an embedding store.
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    @Test
    public void Naive_RAG() {

        // LLM模型
        ChatModel CHAT_MODEL = OpenAiChatModel.builder()
                .apiKey(API_KEY)
                .baseUrl(BASE_URL)
                .modelName("qwen3-max")
                .build();

        // 文档解析器
        DocumentParser documentParser = new TextDocumentParser();
        // 加载文档
        Document document = loadDocument(BASE_PATH + "\\LOL.txt", documentParser);


        // 文档切分器
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        // 嵌入模型
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // 嵌入存储
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        // 内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();


        // 聊天内存
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 助手
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .build();

        String res = assistant.chat("你好，faker拿了多少冠军");
        System.out.println(res);
    }

    @Test
    public void QueryTransformerTest() {
        // 测试查询转换
        QueryTransformer queryTransformer = new ExpandingQueryTransformer(CHAT_MODEL, 4);
        Query query = new Query("你好，faker在今年S赛拿了冠军吗，他一共拿了多少冠军");
        String transformedQuery = queryTransformer.transform(query).toString();
        System.out.println(transformedQuery);
    }

    private static EmbeddingStore<TextSegment> embed(String relativePath, EmbeddingModel embeddingModel) throws URISyntaxException {
        URL fileUrl = Utils.class.getClassLoader().getResource(relativePath);
        Path documentPath = Paths.get(fileUrl.toURI());
        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument(documentPath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    @Test
    public void QueryRouterTest() throws URISyntaxException {

        // Let's create a separate embedding store specifically for biographies.
        // LOL的文档嵌入存储
        EmbeddingStore<TextSegment> LOLEmbeddingStore =
                embed("documents/LOL.txt", EMBEDDING_MODEL);

        // LOL的内容检索器
        ContentRetriever LOLContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(LOLEmbeddingStore)
                .embeddingModel(EMBEDDING_MODEL)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // 歌手的文档嵌入存储
        EmbeddingStore<TextSegment> SingerEmbeddingStore =
                embed("documents/Singer.txt", EMBEDDING_MODEL);
        // 歌手的内容检索器
        ContentRetriever SingerContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(SingerEmbeddingStore)
                .embeddingModel(EMBEDDING_MODEL)
                .maxResults(2)
                .minScore(0.6)
                .build();


        // 构建查询路由映射
        Map<ContentRetriever, String> retrieverToDescription = new HashMap<>();
        retrieverToDescription.put(LOLContentRetriever, "LOL相关的信息");
        retrieverToDescription.put(SingerContentRetriever, "歌手相关的信息");

        // 构建查询路由
        QueryRouter queryRouter = new LanguageModelQueryRouter(CHAT_MODEL, retrieverToDescription);

        // 构建检索增强器
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        String res = assistant.chat("林俊杰的最新新歌是什么");
        System.out.println(res);
    }

    @Test
    public void CompressingQueryTransformerTest() {
        Document document = loadDocument(BASE_PATH + "\\Singer.txt", new TextDocumentParser());


        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(EMBEDDING_MODEL)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);


        QueryTransformer queryTransformer = new CompressingQueryTransformer(CHAT_MODEL);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(EMBEDDING_MODEL)
                .maxResults(2)
                .minScore(0.6)
                .build();


        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // 测试压缩查询转换器
        // 消除代词
        String res1 = assistant.chat("我最近还挺喜欢林俊杰的");
        System.out.println(res1);
        String res2 = assistant.chat("他最近的歌是是什么");
        System.out.println(res2);


    }


}
