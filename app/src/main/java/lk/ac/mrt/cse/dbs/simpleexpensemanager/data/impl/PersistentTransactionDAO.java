package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO extends SQLiteOpenHelper implements TransactionDAO {
    public PersistentTransactionDAO(@Nullable Context context) {
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
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
        cv.put("accountNo", accountNo);
        cv.put("expenseType", expenseType.equals(ExpenseType.EXPENSE)? "EXPENSE" : "INCOME");
        cv.put("amount", amount);

        db.insert("TRANSACTIONS", null,cv);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> transactions = new ArrayList<>();

        String queryString = "SELECT date,accountNo,expenseType,amount from TRANSACTIONS";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()){
            do{
                try {
                    Date date =new SimpleDateFormat("dd-MM-yyyy").parse(cursor.getString(0));

                    String accountNo = cursor.getString(1);
                    String expType = cursor.getString(2);
                    ExpenseType expenseType = expType.equals("EXPENSE") ? ExpenseType.EXPENSE : ExpenseType.INCOME;

                    double amount = cursor.getDouble(3);

                    transactions.add(new Transaction(date, accountNo, expenseType, amount));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return  transactions;
    }

@Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> transactions = getAllTransactionLogs();
        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        // return the last <code>limit</code> number of transaction logs
        return transactions.subList(size - limit, size);
    }

}
