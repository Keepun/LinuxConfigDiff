/*
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
package LinuxConfigDiff;

import java.util.HashMap;
import java.util.LinkedList;

public class KconfigNode
{
    public KconfigNode parent;
    public String path;
    public int type;
    public String value;
    public HashMap<Integer, String> options;
    public LinkedList<KconfigNode> children;

    KconfigNode(int type, String value, String path, KconfigNode parent) {
        this.parent = parent;
        this.path = path;
        this.type = type;
        this.value = value;
        options = new HashMap<Integer, String>();
        children = new LinkedList<KconfigNode>();
    }

    public void addOption(int type, String... value)
    {
        StringBuilder val = new StringBuilder();
        for (String vl : value) {
            if (vl != null) {
                val.append(vl + " ");
            }
        }
        if (options.containsKey(type)) {
            options.put(type, options.get(type) + " " + val.toString().trim());
        } else {
            options.put(type, val.toString().trim());
        }
    }

    public KconfigNode addChild(int type, String value, String path)
    {
        KconfigNode child = new KconfigNode(type, value, path == null ? this.path : path, this);
        children.add(child);
        return child;
    }

    public void addChild(KconfigNode kctree)
    {
        children.add(kctree);
    }
}
