/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kolban.plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.action.Action;
import io.cdap.cdap.etl.api.action.ActionContext;
import io.cdap.cdap.etl.api.action.SettableArguments;

/**
 * Action that moves files from one fileset into another, optionally filtering files that match a regex.
 */
@Plugin(type = Action.PLUGIN_TYPE)
@Name("Anu")
@Description("Action that moves files from one fileset into another, optionally filtering files that match a regex.")
public class AnuAction extends Action {
  private final Conf config;

  /**
   * Config properties for the plugin.
   */
  public static class Conf extends PluginConfig {
    // set defaults for properties in a no-argument constructor.
    public Conf() {
    }
  }

  public AnuAction(Conf config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
  }

  @Override
  public void run(ActionContext context) throws Exception {
    System.out.println("Action: anu called");

    FTPClient ftp = new FTPClient();
    ftp.connect("localhost");
    System.out.println("connect: " + ftp.getReplyString());
    ftp.login("kolban", "password");
    System.out.println("login: " + ftp.getReplyString());
    String names[] = ftp.listNames();
    System.out.println("listNames: " + ftp.getReplyString());
    ftp.disconnect();
    System.out.println("disconnect: " + ftp.getReplyString());

    // ddmmyyyy.csv
    // Find all entries that match [dd][mm][yyyy].csv
    // transform to yyyymmdd
    // sort
    // pick last

    HashMap<String, String> map = new HashMap<>();
    Pattern p = Pattern.compile("(\\d\\d)(\\d\\d)(\\d\\d\\d\\d)\\.csv");
    for (String name: names) {
      Matcher m = p.matcher(name);
      if (m.matches()) {
        String key = m.group(3) + m.group(2) + m.group(1);
        map.put(key, name);
      }
    }
    if (!map.isEmpty()) {
      String nameArray[] = map.keySet().toArray(new String[0]);
      Arrays.sort(nameArray);
      String latestFileKey = nameArray[nameArray.length - 1];
      String latestFile = map.get(latestFileKey);
      System.out.println("Next: " + latestFile);
      SettableArguments args = context.getArguments();
      args.set("inFile", latestFile);
    }
  }
}
