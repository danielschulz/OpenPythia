/**
 * Copyright 2012 msg systems ag
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
 **/
package org.openpythia.dbconnection;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import org.openpythia.schemaprivileges.PrivilegesHelper;
import org.openpythia.utilities.FileSelectorUtility;
import org.openpythia.utilities.PreferencesHandler;

public class DBConnectionParametersController {

    private static DBConnectionParametersController instance;

    private DBConnectionParametersView view;

    private ConnectionPool connectionPool;

    private DBConnectionParametersController() {
        view = new DBConnectionParametersView();

        preFillFields();
        bindActions();

        view.getRootPane().setDefaultButton(view.getBtnOK());

        view.setVisible(true);
    }

    public static boolean establishDBConnection() {
        instance = new DBConnectionParametersController();

        // did the user enter a valid connection?
        return instance.connectionPool != null;
    }

    public static ConnectionPool getConnectionPool() {
        if (instance == null) {
            throw new RuntimeException(
                    "Undefinded condition: Trying to get a connection pool without one beeing intialized.");
        } else {
            return instance.connectionPool;
        }
    }

    private void preFillFields() {
        String host = PreferencesHandler.getHost();
        String port = PreferencesHandler.getPort();
        String databaseName = PreferencesHandler.getDatabaseName();
        String schemaName = PreferencesHandler.getSchemaName();

        if (host == null && port == null && databaseName == null
                && schemaName == null) {
            // no connection available -> preset to Oracle Express Edition
            host = "localhost";
            port = "1521";
            databaseName = "xe";
            schemaName = "pythia";
        }

        view.getTfHost().setText(host);
        view.getTfPort().setText(port);
        view.getTfDatabaseName().setText(databaseName);
        view.getTfSchema().setText(schemaName);

        if (schemaName != null) {
            view.getTfPassword().requestFocus();
        }
    }

    private void bindActions() {
        view.getBtnSchemaCreation().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSchemaCreationScript();
            }
        });
        view.getBtnOK().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleButtonOK();
            }
        });
        view.getBtnCancel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleButtonCancel();
            }
        });
    }

    private void handleButtonCancel() {
        view.dispose();
    }

    private void handleButtonOK() {
        String host = view.getTfHost().getText();
        String port = view.getTfPort().getText();
        String databaseName = view.getTfDatabaseName().getText();
        String schema = view.getTfSchema().getText();
        char[] password = view.getTfPassword().getPassword();

        try {
            connectionPool = new ConnectionPoolImpl(host, port, databaseName,
                    schema, password);

            PreferencesHandler.setHost(host);
            PreferencesHandler.setPort(port);
            PreferencesHandler.setDatabaseName(databaseName);
            PreferencesHandler.setSchemaName(schema);

            view.dispose();

        } catch (SQLException e) {
            // The connection could not be established
            JOptionPane.showMessageDialog(view,
                    "The connection could not be established.\n"
                            + "The error message is " + e.toString());
        } finally {
            // clean the password from memory
            for (int i = 0; i < password.length; i++) {
                password[i] = 0;
            }
        }
    }

    private void generateSchemaCreationScript() {
        final String DEFAULT_SCHEMA_NAME = "pythia";

        File destination = FileSelectorUtility.chooseSQLFileToWrite(view,
                "CreatePythiaSchema.sql");

        if (destination != null) {
            PrintWriter output = null;
            try {
                output = new PrintWriter(new FileOutputStream(destination));

                output.println("-- -----------------------------------------------------------------------------");
                output.println("-- Schema creation script generated by Pythia");
                output.println("-- -----------------------------------------------------------------------------");
                output.println("CREATE USER " + DEFAULT_SCHEMA_NAME
                        + " IDENTIFIED BY \"" + DEFAULT_SCHEMA_NAME + "\";");
                output.println("GRANT create session TO " + DEFAULT_SCHEMA_NAME
                        + ";");
                output.println();

                output.println(PrivilegesHelper.createGrantScript(
                        PrivilegesHelper
                                .getMissingObjectPrivileges((List<String>) null),
                        DEFAULT_SCHEMA_NAME));

                JOptionPane.showMessageDialog(view,
                        "Schema creation script generated.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog((Component) null, e);
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }
    }
}
