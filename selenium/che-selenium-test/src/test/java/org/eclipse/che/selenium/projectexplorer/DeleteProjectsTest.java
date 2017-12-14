/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.EDIT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.IdeMainDockPanel;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Anderey Chizhikov
 */
public class DeleteProjectsTest {

  private static final List<String> PROJECT_NAMES =
      Arrays.asList("Project1", "Project2", "Project3", "Project4", "Project5");

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private Loader loader;
  @Inject private IdeMainDockPanel ideMainDockPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    for (String projectName : PROJECT_NAMES) {
      URL resource = getClass().getResource("/projects/ProjectWithDifferentTypeOfFiles");
      testProjectServiceClient.importProject(
          workspace.getId(), Paths.get(resource.toURI()), projectName, MAVEN_SPRING);
    }
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    consoles.selectProcessByTabName("dev-machine");
  }

  @Test
  public void shouldDeleteProjectByContextMenu() {
    projectExplorer.waitItem(PROJECT_NAMES.get(0));
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAMES.get(0));
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.DELETE);
    acceptDeletion(PROJECT_NAMES.get(0));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(0));
    checkErrorMessageNotPresentInConsole(PROJECT_NAMES.get(0));
  }

  @Test(priority = 1)
  public void shouldDeleteProjectByMenuFile() {
    projectExplorer.waitItem(PROJECT_NAMES.get(1));
    projectExplorer.selectItem(PROJECT_NAMES.get(1));
    menu.runCommand(EDIT, TestMenuCommandsConstants.Edit.DELETE);
    acceptDeletion(PROJECT_NAMES.get(1));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(1));
    checkErrorMessageNotPresentInConsole(PROJECT_NAMES.get(1));
  }

  @Test(priority = 2)
  public void shouldDeleteProjectByDeleteIcon() {
    projectExplorer.waitItem(PROJECT_NAMES.get(2));
    deleteFromDeleteIcon(PROJECT_NAMES.get(2));
    acceptDeletion(PROJECT_NAMES.get(2));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(2));
    checkErrorMessageNotPresentInConsole(PROJECT_NAMES.get(2));
  }

  @Test(priority = 3)
  public void shouldDeleteOpenedProjectByMenuFile() {
    projectExplorer.waitItem(PROJECT_NAMES.get(3));
    projectExplorer.openItemByPath(PROJECT_NAMES.get(3));
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAMES.get(3));
    projectExplorer.selectItem(PROJECT_NAMES.get(3));
    menu.runCommand(EDIT, TestMenuCommandsConstants.Edit.DELETE);
    acceptDeletion(PROJECT_NAMES.get(3));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(3));
    checkErrorMessageNotPresentInConsole(PROJECT_NAMES.get(3));
  }

  @Test(priority = 4)
  public void shouldDeleteOpenedProjectFromContextMenu() {
    projectExplorer.waitItem(PROJECT_NAMES.get(4));
    projectExplorer.openItemByPath(PROJECT_NAMES.get(4));
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAMES.get(4));
    projectExplorer.selectItem(PROJECT_NAMES.get(4));
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAMES.get(4));
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.DELETE);
    acceptDeletion(PROJECT_NAMES.get(4));
    projectExplorer.waitDisappearItemByPath(PROJECT_NAMES.get(4));
    checkErrorMessageNotPresentInConsole(PROJECT_NAMES.get(4));
  }

  private void deleteFromDeleteIcon(String pathToProject) {
    loader.waitOnClosed();
    projectExplorer.selectItem(pathToProject);
    ideMainDockPanel.clickDeleteIcon();
    loader.waitOnClosed();
  }

  private void acceptDeletion(String projectName) {
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    loader.waitOnClosed();
    projectExplorer.waitDisappearItemByPath(projectName);
  }

  private void checkErrorMessageNotPresentInConsole(String projectName) {
    try {
      consoles.waitExpectedTextIntoConsole(projectName, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
      Assert.fail("Error message is present in console");
    } catch (TimeoutException ex) {
    }
  }
}
