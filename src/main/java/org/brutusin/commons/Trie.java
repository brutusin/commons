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

public class Trie<A, B, C> {

    private A element1;
    private B element2;
    private C element3;

    public Trie() {
    }

    public Trie(A element1, B element2, C element3) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
    }

    public A getElement1() {
        return element1;
    }

    public void setElement1(A element1) {
        this.element1 = element1;
    }

    public B getElement2() {
        return element2;
    }

    public void setElement2(B element2) {
        this.element2 = element2;
    }

    public C getElement3() {
        return element3;
    }

    public void setElement3(C element3) {
        this.element3 = element3;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.element1 != null ? this.element1.hashCode() : 0);
        hash = 41 * hash + (this.element2 != null ? this.element2.hashCode() : 0);
        hash = 41 * hash + (this.element2 != null ? this.element3.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Trie)) {
            return false;
        }
        Trie other = (Trie) obj;
        return this.element1.equals(other.getElement1()) && this.element2.equals(other.getElement2()) && this.element3.equals(other.getElement3());
    }

    @Override
    public String toString() {
        return "{" + this.element1 + "," + this.element2 + "," + this.element3 + "}";
    }
}
