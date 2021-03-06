package br.gov.servicos.config;

import br.gov.servicos.metricas.Opiniao;
import br.gov.servicos.servico.Servico;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static br.gov.servicos.foundation.IO.read;
import static lombok.AccessLevel.PRIVATE;

@Component
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class GuiaDeServicosIndex {

    public static final String GDS_IMPORTADOR = "gds-importador";
    public static final String GDS_PERSISTENTE = "gds-persistente";

    private static final String SETTINGS = "/elasticsearch/settings.json";

    ElasticsearchTemplate es;

    @Autowired
    public GuiaDeServicosIndex(ElasticsearchTemplate es) {
        this.es = es;
    }

    private static String settings() throws IOException {
        ClassPathResource resource = new ClassPathResource(SETTINGS);

        return read(resource);
    }

    @CacheEvict(value={"buscas", "conteudo", "destaques", "orgaos", "linhasDaVida", "publicosAlvo"}, allEntries=true)
    public void recriar() throws IOException {
        recriarIndiceImportador();
        criarIndicePersistenteSeNaoExistir();

        es.putMapping(Servico.class);
        es.putMapping(Opiniao.class);
    }

    private void criarIndicePersistenteSeNaoExistir() throws IOException {
        if (!es.indexExists(GDS_PERSISTENTE))
            es.createIndex(GDS_PERSISTENTE, settings());
    }

    private void recriarIndiceImportador() throws IOException {
        if (es.indexExists(GDS_IMPORTADOR)) {
            es.deleteIndex(GDS_IMPORTADOR);
        }
        es.createIndex(GDS_IMPORTADOR, settings());
    }

}
