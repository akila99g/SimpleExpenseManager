/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.ExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.PersistentExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class ApplicationTest {
    private static ExpenseManager expenseManager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        expenseManager = new PersistentExpenseManager(context);
    }

    @Test
    public void testAddAccount() {
        //add new account
        expenseManager.addAccount("190239A","BOC","Akila",10000.00);
        //get the accounts list
        List<String> accNums = expenseManager.getAccountNumbersList();

        //check the retrieved list contains added account
        assertTrue(accNums.contains("190239A"));
    }

    @Test
    public void  testLogTransaction(){
        expenseManager.addAccount("190239A","BOC","Akila",10000.00);
        try {
            //log new transaction
            expenseManager.updateAccountBalance("190239A",10,4,2022, ExpenseType.EXPENSE,"1000.00"); //month May=4 (indexing)

            //get transactions list
            List<Transaction> transactions = expenseManager.getTransactionLogs();
            //filter out last transaction
            Transaction lastTransaction = transactions.get(transactions.size()-1);

            //compare the values of the retrieved transaction
            assertEquals(1000.00, lastTransaction.getAmount(), 0.0);
            assertEquals(ExpenseType.EXPENSE,lastTransaction.getExpenseType());
            assertEquals("190239A",lastTransaction.getAccountNo());
            String retrievedDate = new SimpleDateFormat("dd-MM-yyyy").format(lastTransaction.getDate());
            assertEquals("10-05-2022",retrievedDate);

        } catch (InvalidAccountException e) {
            e.printStackTrace();
        }
    }

}