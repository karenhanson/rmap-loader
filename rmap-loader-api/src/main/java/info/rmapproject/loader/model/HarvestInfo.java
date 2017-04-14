/*
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.rmapproject.loader.model;

import java.net.URI;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author apb@jhu.edu
 */
public class HarvestInfo {

    @JsonProperty("@id")
    URI id;

    @JsonProperty("@type")
    private final URI type = URI
            .create("http://rmap-project.org/types/HarvestInfo");

    URI src;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    Date date;

    /** Unique identifier for this record */
    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    /** URI which identifies where the record came from or was derived from */
    public URI getSrc() {
        return src;
    }

    public void setSrc(URI src) {
        this.src = src;
    }

    /** Date at which the record was generated by the extractor */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
