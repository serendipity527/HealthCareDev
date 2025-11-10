package com.yihu.agent;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

@SpringBootTest
public class PgvectorTest {


    @Test
    @Rollback(false)
    void PgvectorRagTest() {
        // 1. Embedding Model
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        // 2. 创建 PgVectorEmbeddingStore 实例
        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                .host("117.72.176.184")                           // Required: Host of the PostgreSQL instance
                .port(5432)                                  // Required: Port of the PostgreSQL instance
                .database("healthcare")                        // Required: Database name
                .user("pgvector")                             // Required: Database user
                .password("pgvector")                     // Required: Database password
                .table("test_embeddings")                      // Required: Table name to store embeddings
                .dimension(embeddingModel.dimension())       // Required: Dimension of embeddings
                // Optional parameters
                .useIndex(true)                             // Enable IVFFlat index
                .indexListSize(100)                         // Number of lists for IVFFlat index
                .createTable(true)                          // Automatically create the table if it doesn’t exist
                .dropTableFirst(false)                      // Don’t drop the table first (set to true if you want a fresh start)
//                .metadataStorageConfig(MetadataStorageConfig.combinedJsonb()) // Store metadata as a combined JSONB column

                .build();


        // 3. 存入数据 (Ingest)
        TextSegment segment1 = TextSegment.from("小明喜欢吃苹果。");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("小红喜欢吃香蕉。");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);



        // 5. 搜索相似数据 (Search)
        Embedding queryEmbedding = embeddingModel.embed("小明喜欢吃什么水果？").content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(10)
                .build();
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();

        System.out.println(relevant.size());

        EmbeddingMatch<TextSegment> match = relevant.get(0);
        System.out.println("最相关的内容: " + match.embedded().text());
        System.out.println("相似度分数: " + match.score());
    }

}
