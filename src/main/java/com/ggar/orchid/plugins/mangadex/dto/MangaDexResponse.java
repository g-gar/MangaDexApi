package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MangaDexResponse<T> {
    @JsonProperty("result") private String result;
    @JsonProperty("response") private String responseType;
    @JsonProperty("data") private T data;
    @JsonProperty("limit") private Integer limit;
    @JsonProperty("offset") private Integer offset;
    @JsonProperty("total") private Integer total;
    @JsonProperty("errors") private List<ErrorDetail> errors;
    public String getResult() { return result; } public void setResult(String result) { this.result = result; }
    public String getResponseType() { return responseType; } public void setResponseType(String responseType) { this.responseType = responseType; }
    public T getData() { return data; } public void setData(T data) { this.data = data; }
    public Integer getLimit() { return limit; } public void setLimit(Integer limit) { this.limit = limit; }
    public Integer getOffset() { return offset; } public void setOffset(Integer offset) { this.offset = offset; }
    public Integer getTotal() { return total; } public void setTotal(Integer total) { this.total = total; }
    public List<ErrorDetail> getErrors() { return errors; } public void setErrors(List<ErrorDetail> errors) { this.errors = errors; }
}
