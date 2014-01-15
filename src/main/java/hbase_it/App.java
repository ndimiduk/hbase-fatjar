package hbase_it;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.RowCounter;
import org.apache.hadoop.hbase.util.AbstractHBaseTool;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;

public class App extends AbstractHBaseTool {
  private String tableName;
  private byte[] fam = Bytes.toBytes("a");
  private byte[] ZERO = Bytes.toBytes(0);
  private int numRows;
  private boolean isWriteJob;

  @Override
  protected void addOptions() {
    addOptWithArg("t", "table", "Table name. Default: 'TestTable'");
    addOptWithArg("n", "numRows", "The number of rows to use. Default: 1000");
    addOptNoArg("w", "write", "Specify to populate the table. Executes read job otherwise.");
  }

  @Override
  protected void processOptions(CommandLine cmd) {
    this.tableName = cmd.getOptionValue("table", "TestTable");
    this.numRows = Integer.parseInt(cmd.getOptionValue("numRows", "1000"));
    this.isWriteJob = cmd.hasOption("write");
  }

  @Override
  protected int doWork() throws Exception {
    Job job = null;
    if (isWriteJob) {
      HConnection conn = null;
      HBaseAdmin admin = null;
      HTableInterface table = null;
      try {
        conn = HConnectionManager.createConnection(getConf());
        admin = new HBaseAdmin(conn);
        if (admin.tableExists(tableName)) {
          admin.disableTable(tableName);
          admin.deleteTable(tableName);
        }
        HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(tableName));
        HColumnDescriptor col = new HColumnDescriptor("a");
        htd.addFamily(col);
        admin.createTable(htd);

        table = conn.getTable(tableName);
        for (int i = 0; i < numRows; i++) {
          byte[] iAsBytes = Bytes.toBytes(i);
          Put p = new Put(iAsBytes);
          p.add(fam, ZERO, iAsBytes);
          table.put(p);
        }
        return 0;
      } finally {
        if (table != null) table.close();
        if (admin != null) admin.close();
        if (conn != null) conn.close();
      }
    } else {
      // be lazy
      job = RowCounter.createSubmittableJob(getConf(), new String[] {this.tableName});
      if (job == null) return -1;
      job.setJobName("HBase fat jar smoketest: " + (isWriteJob ? "write" : "read"));
      job.setJarByClass(this.getClass());
      job.waitForCompletion(true);
      return 0;
    }
  }

  public static void main(String[] args) throws Exception {
    new App().doStaticMain(args);
  }
}
