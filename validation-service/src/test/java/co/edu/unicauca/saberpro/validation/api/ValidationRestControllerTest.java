package co.edu.unicauca.saberpro.validation.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void preguntaValidaResponde200ConValidaTrue() throws Exception {
        String json = """
                {"enunciado": "¿Qué patrón aísla el dominio?",
                 "justificacion": "Los puertos y adaptadores aíslan el núcleo.",
                 "opciones": [{"texto": "Hexagonal", "esCorrecta": true},
                              {"texto": "Singleton", "esCorrecta": false},
                              {"texto": "Monolito", "esCorrecta": false},
                              {"texto": "Capas", "esCorrecta": false}]}
                """;

        mvc.perform(post("/api/validaciones").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(true))
                .andExpect(jsonPath("$.errores", hasSize(0)));
    }

    @Test
    void preguntaInvalidaDevuelveLosErrores() throws Exception {
        String json = """
                {"enunciado": "Enunciado", "justificacion": "corta",
                 "opciones": [{"texto": "A", "esCorrecta": true}]}
                """;

        mvc.perform(post("/api/validaciones").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(false))
                .andExpect(jsonPath("$.errores", hasSize(2)));
    }
}
