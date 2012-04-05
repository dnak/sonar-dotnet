/*
 * .NET tools :: DependencyParser Runner
 * Copyright (C) 2010 Jose Chillan, Alexandre Victoor and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.dotnet.tools.dependencyparser;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.command.Command;
import org.sonar.dotnet.tools.commons.visualstudio.VisualStudioProject;
import org.sonar.test.TestUtils;

public class DependencyParserCommandBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static File dependencyParserExecutable;
  private static File dependencyParserReportFile;
  private VisualStudioProject vsProject;
  private DependencyParserCommandBuilder dependencyParserCommandBuilder;

  @BeforeClass
  public static void initStatic() throws Exception {
    dependencyParserExecutable = TestUtils.getResource("/Runner/FakeProg/DependencyParser.exe");
    dependencyParserReportFile = new File("target/sonar/Deps/deps-report.xml");
  }

  @Before
  public void init() throws Exception {
    vsProject = mock(VisualStudioProject.class);
    when(vsProject.getArtifact("Debug")).thenReturn(TestUtils.getResource("/Runner/FakeAssemblies/Fake1.assembly"));
    dependencyParserCommandBuilder = DependencyParserCommandBuilder.createBuilder(null, vsProject);
    dependencyParserCommandBuilder.setExecutable(dependencyParserExecutable);
    dependencyParserCommandBuilder.setReportFile(dependencyParserReportFile);
  }

  @Test
  public void testToCommandForVSProject() throws Exception {
    Command command = dependencyParserCommandBuilder.toCommand();
    assertThat(toUnixStyle(command.getExecutable()), endsWith("/Runner/FakeProg/DependencyParser.exe"));
    String[] commands = command.getArguments().toArray(new String[] {});
    assertThat(commands[0], is("-a"));
    assertThat(commands[1], endsWith("Fake1.assembly"));
    assertThat(commands[2], is("-o"));
    assertThat(commands[3], endsWith("deps-report.xml"));
  }

  @Test
  public void testToCommandWithNoAssembly() throws Exception {
    when(vsProject.getArtifact("Debug")).thenReturn(null);

    thrown.expect(DependencyParserException.class);
    thrown.expectMessage("Assembly to scan not found for project");
    dependencyParserCommandBuilder.toCommand();
  }

  @Test
  public void testToCommandWithUnexistingAssembly() throws Exception {
    when(vsProject.getArtifact("Debug")).thenReturn(new File("target/sonar/Deps/unexisting-assembly.dll"));

    thrown.expect(DependencyParserException.class);
    thrown.expectMessage("Assembly to scan not found for project");
    dependencyParserCommandBuilder.toCommand();
  }

  private String toUnixStyle(String path) {
    return path.replaceAll("\\\\", "/");
  }

}