/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express
 * or implied. See the License for the specific language governing permissions
 * and limitations under
 * the License.
 */

import java.utils.Arrays;

public class Runner {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("error: no inputs");
            System.exit(1);
        }
        var cmdName = args[0];
        var cmdArgs = Arrays.copyOfRange(args, 1, args.length - 1);
        Class<?> cls = Class.forName(cmdName);
        Constructor<?> ctor = cls.getConstructor();
        Runnable cmd = (Runnable) ctor.newInstance();
        cmd.run(cmdArgs);
    }
}
