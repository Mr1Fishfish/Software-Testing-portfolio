package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.acp.cw2.controller.ServiceController;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

//@SpringBootTest
@WebMvcTest(ServiceController.class)
class AcpCw2ApplicationTests {

    @Test
    void contextLoads() {
    }


    @Autowired
    private MockMvc mockMvc;
    @Test
    public void testGetUser() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())  //  200
                .andExpect(content().string("s1802871"));  // return "s1802871"
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/v1/actuator/health"))
                .andExpect(status().isOk())  // 200
                .andExpect(jsonPath("$.status", is("UP")));
    }


    @Test
    void testDistanceTo() throws Exception {
        String body = """
            {
              "position1": {"lng": -3.192473, "lat": 55.946233},
              "position2": {"lng": -3.192473, "lat": 55.942617}
            }
            """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", comparesEqualTo(new BigDecimal("0.003616000000000952"))));
    }
    @Test
    void testDistanceToPosition1Null() throws Exception {
        String body = """
            {
             "position1": {"lng": -3.192473, "lat": 55.946233}
            }
            """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testDistanceToPosition2Null() throws Exception {
        String body = """
            {
              "position2": {"lng": -3.192473, "lat": 55.942617}
            }
            """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testDistanceToPosition1LngNull() throws Exception {
        String body = """
            {
               "position1": { "lat": 55.946233},
               "position2": {"lng": -3.192473, "lat": 55.942617}
            }
            """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testDistanceToPosition1LatNull() throws Exception {
        String body = """
            {
               "position1": {"lng": -3.192473},
               "position2": {"lng": -3.192473, "lat": 55.942617}
            }
            """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testDistanceToPosition2LngNull() throws Exception {
        String body = """
            {
               "position1": {"lng": -3.192473,"lat": 55.942617},
               "position2": { "lat": 55.942617}
            }
            """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testDistanceToPosition2LatNull() throws Exception {
        String body = """
            {
               "position1": {"lng": -3.192473,"lat": 55.942617},
               "position2": {"lng": -3.192473}
            }
            """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testIsCloseToTure() throws Exception {
        String body = """
                {
                  "position1": {
                  "lng": -3.192473,
                  "lat": 55.942657
                  },
                  "position2": {
                  "lng": -3.192473,
                  "lat": 55.942617
                  }
                 }
            """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));

    }
    @Test
    void testIsCloseToFalse() throws Exception {
        String body = """
                {
                  "position1": {
                  "lng": -3.192473,
                  "lat": 55.942817
                  },
                  "position2": {
                  "lng": -3.192473,
                  "lat": 55.942617
                  }
                 }
            """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));

    }

    @Test
    void testNextPosition() throws Exception {
        String body = """
                {
                   "start": {
                   "lng": -3.192473,
                   "lat": 55.946233
                   },
                   "angle": 45
                  }
            """;
        String body_Result = """
                {
                     "lng": -3.192367,
                     "lat": 55.946339
                 }
            """;


        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json(body_Result));

    }
    @Test
    void testNextPositionLngAndLatNull() throws Exception {
        String body = """
                {
                  "angle": 45
                 }
            """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testNextPositionLngNull() throws Exception {
        String body = """
                {
                       "start": {
                       "lat": 55.946233
                       },
                       "angle": 45
                      }
            """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testNextPositionLatNull() throws Exception {
        String body = """
                {
                            "start": {
                            "lng": -3.192473,
                            },
                            "angle": 45
                           }
            """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testNextPositionAngleNull() throws Exception {
        String body = """
                {
                     "start": {
                     "lng": -3.192473,
                     "lat": 55.946233
                     },
                     },
                    }
            """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }


    @Test
    void testIsInRegionFalse() throws Exception {
        String body = """
                {
                   "position": {
                   "lng": 1.234,
                   "lat": 1.222
                   },
                   "region": {
                   "name": "central",
                   "vertices": [
                   {
                   "lng": -3.192473,
                   "lat": 55.946233
                   },
                   {
                   "lng": -3.192473,
                   "lat": 55.942617
                   },
                   {
                   "lng": -3.184319,
                   "lat": 55.942617
                   },
                   {
                   "lng": -3.184319,
                   "lat": 55.946233
                   },
                   {
                   "lng": -3.192473,
                   "lat": 55.946233
                   }
                   ]
                   }
                  }
            """;
        String body_Result = """
                {
                          "isInRegion": false
                      }
            """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json(body_Result));

    }

    @Test
    void testIsInRegionTrue() throws Exception {
        String body = """
                {
                   "position": {
                   "lng": -3.192473,
                   "lat": 55.946233
                   },
                   "region": {
                   "name": "central",
                   "vertices": [
                   {
                   "lng": -3.192473,
                   "lat": 55.946233
                   },
                   {
                   "lng": -3.192473,
                   "lat": 55.942617
                   },
                   {
                   "lng": -3.184319,
                   "lat": 55.942617
                   },
                   {
                   "lng": -3.184319,
                   "lat": 55.946233
                   },
                   {
                   "lng": -3.192473,
                   "lat": 55.946233
                   }
                   ]
                   }
                  }
            """;
        String body_Result = """
                {
                          "isInRegion": true
                      }
            """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json(body_Result));

    }

    @Test
    void testIsInRegionPositionNull() throws Exception {
        String body = """
                {
           
                          "region": {
                          "name": "central",
                          "vertices": [
                          {
                          "lng": -3.192473,
                          "lat": 55.946233
                          },
                          {
                          "lng": -3.192473,
                          "lat": 55.942617
                          },
                          {
                          "lng": -3.184319,
                          "lat": 55.942617
                          },
                          {
                          "lng": -3.184319,
                          "lat": 55.946233
                          },
                          {
                          "lng": -3.192473,
                          "lat": 55.946233
                          }
                          ]
                          }
                         }
            """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testIsInRegionRegionNull() throws Exception {
        String body = """
                {
                    "position": {
                    "lng": 1.234,
                    "lat": 1.222
                    },
           
                    "vertices": [
                    {
                    "lng": -3.192473,
                    "lat": 55.946233
                    },
                    {
                    "lng": -3.192473,
                    "lat": 55.942617
                    },
                    {
                    "lng": -3.184319,
                    "lat": 55.942617
                    },
                    {
                    "lng": -3.184319,
                    "lat": 55.946233
                    },
                    {
                    "lng": -3.192473,
                    "lat": 55.946233
                    }
                    ]
                    }
                   }
            """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testIsInRegionVerticesNull() throws Exception {
        String body = """
                {
                  "position": {
                  "lng": 1.234,
                  "lat": 1.222
                  },
                  "region": {
                  "name": "central"
                 }
                 }
            """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }
    @Test
    void testIsInRegionLngMissingInPositionNull() throws Exception {
        String body = """
                {
                       "position": {
                       "lat": 1.222
                       },
                       "region": {
                       "name": "central",
                       "vertices": [
                       {
                       "lng": -3.192473,
                       "lat": 55.946233
                       },
                       {
                       "lng": -3.192473,
                       "lat": 55.942617
                       },
                       {
                       "lng": -3.184319,
                       "lat": 55.942617
                       },
                       {
                       "lng": -3.184319,
                       "lat": 55.946233
                       },
                       {
                       "lng": -3.192473,
                       "lat": 55.946233
                       }
                       ]
                       }
                      }
            """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testIsInRegionLngMissingInVerticesNull() throws Exception {
        String body = """
                    {
                       "position": {
                       "lng": 1.234,
                       "lat": 1.222
                       },
                       "region": {
                       "name": "central",
                       "vertices": [
                       {
                       "lat": 55.946233
                       },
                       {
                       "lng": -3.192473,
                       "lat": 55.942617
                       },
                       {
                       "lng": -3.184319,
                       "lat": 55.942617
                       },
                       {
                       "lng": -3.184319,
                       "lat": 55.946233
                       },
                       {
                       "lng": -3.192473,
                       "lat": 55.946233
                       }
                       ]
                       }
                      }
                """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }




    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/v1/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Welcome from ILP</h1>")))
                .andExpect(content().string(containsString("<h4>ILP-REST-Service-URL:</h4>")));
    }












}