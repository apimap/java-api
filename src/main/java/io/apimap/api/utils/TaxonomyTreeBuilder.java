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

import io.apimap.api.rest.TaxonomyDataRestEntity;
import io.apimap.api.rest.TaxonomyTreeDataRestEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TaxonomyTreeBuilder {
    protected ArrayList<TaxonomyTreeDataRestEntity> tree;
    public TaxonomyTreeBuilder() {
        this.tree = new ArrayList<>();
    }

    public static TaxonomyTreeBuilder empty() {
        return new TaxonomyTreeBuilder();
    }

    public ArrayList<TaxonomyTreeDataRestEntity> getTree() {
        return tree;
    }

    public void setTree(ArrayList<TaxonomyTreeDataRestEntity> tree) {
        this.tree = tree;
    }

    public boolean insert(TaxonomyTreeDataRestEntity newEntity) {
        if(newEntity.getReferenceType() == TaxonomyDataRestEntity.ReferenceType.REFERENCE){
            return false;
        }

        List<String> urlParts = splitURLParts(newEntity.getUrl());
        recursiveInsert(tree, newEntity, createInitialURL(urlParts), new ArrayList<>(splitURLParts(newEntity.getUrl())));

        return true;
    }

    protected String createInitialURL(List<String> urlParts){
        return "taxonomy://" + urlParts.get(0);
    }

    protected void replaceExistingNodeInformation(TaxonomyTreeDataRestEntity node, TaxonomyTreeDataRestEntity newEntity){
        // Replace root if empty
        if (node.getUrl().equalsIgnoreCase(newEntity.getUrl())) {
            node.setDescription(newEntity.getDescription());
            node.setTitle(newEntity.getTitle());
            node.setUri(newEntity.getUri());
            node.setUrl(newEntity.getUrl());
            node.setUrn(newEntity.getUrn());
        }
    }

    protected void recursiveInsert(ArrayList<TaxonomyTreeDataRestEntity> tree, TaxonomyTreeDataRestEntity node, String url, ArrayList<String> paths) {
        Optional<TaxonomyTreeDataRestEntity> deficientNode = tree
                .stream()
                .filter(e -> e.getUrl().toLowerCase().startsWith(url.toLowerCase()))
                .findAny();

        if (deficientNode.isPresent()) {
            if (deficientNode.get().getUrl().equalsIgnoreCase(node.getUrl())) {
                deficientNode.get().setDescription(node.getDescription());
                deficientNode.get().setTitle(node.getTitle());
                deficientNode.get().setUri(node.getUri());
                deficientNode.get().setUrl(node.getUrl());
                deficientNode.get().setUrn(node.getUrn());
                deficientNode.get().setReferenceType(node.getReferenceType());
            }else{
                recursiveInsert(deficientNode.get().getEntities(), node, url + "/" + paths.get(1), new ArrayList<>(paths.subList(1, paths.size())));
            }
        }else{
            TaxonomyTreeDataRestEntity newNode;

            if(url.toLowerCase().equals(node.getUrl().toLowerCase())){
                newNode = new TaxonomyTreeDataRestEntity(
                        node.getUrn(),
                        node.getTitle(),
                        node.getUrl(),
                        node.getDescription(),
                        node.getUri(),
                        "1",
                        node.getReferenceType(),
                        new ArrayList<>(Arrays.asList())
                );
            }else{
                newNode = new TaxonomyTreeDataRestEntity(
                        null,
                        null,
                        url,
                        null,
                        null,
                        "1",
                        TaxonomyDataRestEntity.ReferenceType.UNKNOWN,
                        new ArrayList<>(Arrays.asList())
                );
            }

            tree.add(newNode);

            // Recursive loop breaker
            if (paths.size() > 1) {
                recursiveInsert(newNode.getEntities(), node, url + "/" + paths.get(1), new ArrayList<>(paths.subList(1, paths.size())));
            }
        }
    }

    /*
    * taxonomy://First/Second/ -> First/Second
    */
    protected List<String> splitURLParts(String url) {
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