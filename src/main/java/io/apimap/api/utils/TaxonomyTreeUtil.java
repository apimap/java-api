/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package io.apimap.api.utils;

import io.apimap.api.rest.TaxonomyTreeDataRestEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TaxonomyTreeUtil {
    TaxonomyTreeDataRestEntity rootEntity = new TaxonomyTreeDataRestEntity();

    public static TaxonomyTreeUtil empty() {
        return new TaxonomyTreeUtil();
    }

    public TaxonomyTreeDataRestEntity getRootEntity() {
        return rootEntity;
    }

    public TaxonomyTreeUtil insert(TaxonomyTreeDataRestEntity newEntity) {
        List<String> urlParts = urlParts(newEntity.getUrl());
        insert(this.rootEntity, newEntity, "taxonomy://" + urlParts.get(0), urlParts);
        return this;
    }

    protected void insert(TaxonomyTreeDataRestEntity rootEntity, TaxonomyTreeDataRestEntity newEntity, String url, List<String> paths) {
        Optional<TaxonomyTreeDataRestEntity> found = rootEntity
                .getEntities()
                .stream()
                .filter(e -> url.toLowerCase().startsWith(e.getUrl().toLowerCase()))
                .findAny();

        // Replace root if empty
        if (found.isPresent()) {
            if (found.get().getUrl().equalsIgnoreCase(newEntity.getUrl())) {
                found.get().setDescription(newEntity.getDescription());
                found.get().setTitle(newEntity.getTitle());
                found.get().setUri(newEntity.getUri());
                found.get().setUrl(newEntity.getUrl());
                found.get().setUrn(newEntity.getUrn());
                return;
            }
        }

        TaxonomyTreeDataRestEntity tree = recursiveInsert(newEntity, url, new ArrayList<>(urlParts(newEntity.getUrl())));
        TaxonomyTreeDataRestEntity initialTree = tree;
        TaxonomyTreeDataRestEntity initialRootEntity = rootEntity;

        // Merge tree
        try {
            while (tree != null) {
                if (tree.getEntities() == null
                        || tree.getEntities().size() == 0
                        || tree.getEntities().get(0).getUrl().equalsIgnoreCase(rootEntity.getUrl())) {
                    break;
                }

                tree = tree.getEntities() != null ? tree.getEntities().get(0) : null;

                if (tree != null) {
                    TaxonomyTreeDataRestEntity finalTree = tree;
                    rootEntity = rootEntity
                            .getEntities()
                            .stream()
                            .filter(e -> finalTree.getUrl().toLowerCase().startsWith(e.getUrl().toLowerCase()))
                            .findAny()
                            .orElse(null);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        if(rootEntity == null) {
            initialRootEntity.getEntities().add(initialTree);
        }else{
            rootEntity.getEntities().add(tree);
        }
    }

    private TaxonomyTreeDataRestEntity recursiveInsert(TaxonomyTreeDataRestEntity node, String url, ArrayList<String> paths) {
        if (paths.size() == 1) {
            return new TaxonomyTreeDataRestEntity(
                    node.getUrn(),
                    node.getUrl(),
                    node.getTitle(),
                    node.getDescription(),
                    node.getUri()
            );
        }

        TaxonomyTreeDataRestEntity element = recursiveInsert(node, url + "/" + paths.get(0), new ArrayList<>(paths.subList(1, paths.size())));

        return new TaxonomyTreeDataRestEntity(
                null,
                url,
                null,
                null,
                null,
                new ArrayList<>(Arrays.asList(element))
        );
    }

    protected List<String> urlParts(String url) {
        url = url.toLowerCase();

        // Strip elements
        if (url.startsWith("taxonomy://")) {
            url = url.substring(11);
        }

        if (url.startsWith("/")) {
            url = url.substring(1);
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return new ArrayList<>(Arrays.asList(url.split("/")));
    }
}