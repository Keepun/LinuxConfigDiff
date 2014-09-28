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
    public String type;
    public String value;
    public HashMap<String, String> options;
    public LinkedList<KconfigNode> children;

    KconfigNode(String type, String value, String path, KconfigNode parent) {
        this.parent = parent;
        this.path = path;
        this.type = type;
        this.value = value;
        options = new HashMap<String, String>();
        children = new LinkedList<KconfigNode>();
    }

    public void addOption(String key, String value)
    {
        if (options.containsKey(key)) {
            options.put(key, options.get(key) + " " + value);
        } else {
            options.put(key, value);
        }
    }

    public KconfigNode addChild(String type, String value, String path)
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
