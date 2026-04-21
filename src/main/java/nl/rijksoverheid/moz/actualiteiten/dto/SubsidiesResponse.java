package nl.rijksoverheid.moz.actualiteiten.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SubsidiesResponse {
    @NotNull
    public List<EnrichedSubsidie> subsidies;
    public int total;
}
