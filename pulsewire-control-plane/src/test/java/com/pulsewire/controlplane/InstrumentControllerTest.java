package com.pulsewire.controlplane;

import com.pulsewire.controlplane.controller.InstrumentController;
import com.pulsewire.controlplane.entity.Instrument;
import com.pulsewire.controlplane.service.InstrumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstrumentController.class)
class InstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstrumentService instrumentService;

    @Test
    void shouldReturnAllInstruments() throws Exception {
        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setInstrumentId("AAPL-NYSE");
        instrument.setSymbol("AAPL");
        instrument.setVenue("NYSE");

        when(instrumentService.findAll()).thenReturn(List.of(instrument));

        mockMvc.perform(get("/api/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instrumentId").value("AAPL-NYSE"))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }

    @Test
    void shouldReturnInstrumentById() throws Exception {
        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setInstrumentId("GOOG-NASDAQ");
        instrument.setSymbol("GOOG");
        instrument.setVenue("NASDAQ");

        when(instrumentService.findById(1L)).thenReturn(Optional.of(instrument));

        mockMvc.perform(get("/api/instruments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrumentId").value("GOOG-NASDAQ"));
    }

    @Test
    void shouldReturn404WhenInstrumentNotFound() throws Exception {
        when(instrumentService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/instruments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateInstrument() throws Exception {
        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setInstrumentId("MSFT-NASDAQ");
        instrument.setSymbol("MSFT");
        instrument.setVenue("NASDAQ");

        when(instrumentService.create(any(Instrument.class))).thenReturn(instrument);

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "instrumentId": "MSFT-NASDAQ",
                                    "symbol": "MSFT",
                                    "venue": "NASDAQ"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.instrumentId").value("MSFT-NASDAQ"));
    }
}
