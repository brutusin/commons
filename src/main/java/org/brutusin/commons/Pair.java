/*
 * Copyright 2014 brutusin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons;

public class Pair<K, V> {

    private K element1;
    private V element2;

    public Pair() {
    }

    public Pair(K element1, V element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    public K getElement1() {
        return this.element1;
    }

    public void setElement1(K element1) {
        this.element1 = element1;
    }

    public V getElement2() {
        return this.element2;
    }

    public void setElement2(V element2) {
        this.element2 = element2;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.element1 != null ? this.element1.hashCode() : 0);
        hash = 41 * hash + (this.element2 != null ? this.element2.hashCode() : 0);
        return hash;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair other = (Pair) obj;
        return this.element1.equals(other.getElement1()) && this.element2.equals(other.getElement2());
    }

    @Override
    public String toString() {
        return "{" + this.element1 + "," + this.element2 + "}";
    }
}
