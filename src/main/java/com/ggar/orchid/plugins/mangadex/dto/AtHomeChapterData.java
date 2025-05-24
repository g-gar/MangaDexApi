package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AtHomeChapterData { 
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("data")
    private List<String> data; 
    @JsonProperty("dataSaver")
    private List<String> dataSaver; 

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public List<String> getData() { return data; }
    public void setData(List<String> data) { this.data = data; }
    public List<String> getDataSaver() { return dataSaver; }
    public void setDataSaver(List<String> dataSaver) { this.dataSaver = dataSaver; }
}
