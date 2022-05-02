package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO extends SQLiteOpenHelper implements AccountDAO{


    public PersistentAccountDAO(@Nullable Context context) {
        super(context, "190239A.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String create_table_statement1 = "CREATE TABLE 'ACCOUNTS' ('accountNo' TEXT PRIMARY KEY, 'bankName' TEXT NOT NULL, 'accountHolderName' TEXT NOT NULL, 'balance' REAL NOT NULL )";
        sqLiteDatabase.execSQL(create_table_statement1);

        String create_table_statement2 = "CREATE TABLE 'TRANSACTIONS'(\n" +
                "                'transaction_id' INTEGER PRIMARY KEY,\n" +
                "                'date' TEXT NOT NULL,\n" +
                "                'accountNo' TEXT NOT NULL,\n" +
                "                'expenseType' TEXT NOT NULL,\n" +
                "                'amount' REAL NOT NULL,\n" +
                "                FOREIGN KEY ('accountNo')\n" +
                "                REFERENCES 'ACCOUNTS' ('accountNo')\n" +
                "                ON DELETE CASCADE\n" +
                "                ON UPDATE CASCADE\n" +
                "        )";

        sqLiteDatabase.execSQL(create_table_statement2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public List<String> getAccountNumbersList() {
        List<String> accNums = new ArrayList<>();

        String queryString = "SELECT accountNo from ACCOUNTS";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                String accNum = cursor.getString(0);
                accNums.add(accNum);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return  accNums;
    }

    @Override
    public List<Account> getAccountsList() {
        List<Account> accs = new ArrayList<>();

        String queryString = "SELECT * from ACCOUNTS";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()){
            do{
                String accNum = cursor.getString(0);
                String bankName = cursor.getString(1);
                String accHolderName = cursor.getString(2);
                double balance = cursor.getDouble(3);

                accs.add(new Account(accNum,bankName, accHolderName, balance));
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return  accs;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ACCOUNTS WHERE accountNo = ?", new String[] {accountNo});

        if(cursor.moveToFirst()){
            String accNum = cursor.getString(0);
            String bankName = cursor.getString(1);
            String accHolderName = cursor.getString(2);
            double balance = cursor.getDouble(3);

            Account newAcc = new Account(accNum,bankName, accHolderName, balance);

            cursor.close();
            db.close();
            return newAcc;
        }
        else {
            String msg = "Account " + accountNo + " is invalid.";

            cursor.close();
            db.close();
            throw new InvalidAccountException(msg);
        }
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("accountNo", account.getAccountNo());
        cv.put("bankName", account.getBankName());
        cv.put("accountHolderName", account.getAccountHolderName());
        cv.put("balance", account.getBalance());

        db.insertWithOnConflict("ACCOUNTS", null,cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("ACCOUNTS","accountNo=?", new String[]{accountNo});

        db.close();

    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {

        Account account = getAccount(accountNo);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        Double balance = account.getBalance();

        Double newBalance = expenseType == ExpenseType.EXPENSE ? balance - amount : balance + amount;

        cv.put("accountNo", accountNo);
        cv.put("bankName", account.getBankName());
        cv.put("accountHolderName", account.getAccountHolderName());
        cv.put("balance", newBalance);

        db.updateWithOnConflict("ACCOUNTS", cv, "accountNo=?", new String[]{accountNo},SQLiteDatabase.CONFLICT_IGNORE);
    }
}