package mr;

import base.SystemBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

@Slf4j
public class DataNoHeavy {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        SystemBase.setOS();
        Configuration conf = new Configuration();
        GenericOptionsParser optionsParser = new GenericOptionsParser(conf, args);
        String[] remainingArgs = optionsParser.getRemainingArgs();
        //新建一个Job工作
        Job job = new Job(conf);
        //设置运行类
        //job.setJarByClass(JobRun.class);
        //设置要执行的mapper类（自己书写的）
        job.setMapperClass(DataMap.class);
        //设置要执行的reduce类（自己书写的）
        job.setReducerClass(DataReduce.class);
        //设置输出key的类型
        job.setMapOutputKeyClass(Text.class);
        //设置输出value的类型
        job.setMapOutputValueClass(Text.class);

        //设置ruduce任务的个数，默认个数为一个（一般reduce的个数越多效率越高）
        job.setNumReduceTasks(1);

        //mapreduce 输入数据的文件/目录
        FileInputFormat.addInputPath(job, new Path("/usr/input/wc/Demo/dataNoHeavy"));
        //mapreduce 执行后输出的数据目录
        FileOutputFormat.setOutputPath(job, new Path("/usr/output/Demo/dataNoHeavy"));
        //执行完毕退出
        System.exit(job.waitForCompletion(true) ? 0:1);
    }


    public static class DataMap extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws java.io.IOException, InterruptedException {
            Text line = value;
            context.write(line, new Text(""));
        }
    }

    private static class DataReduce extends Reducer<Text, Text, Text, Text> implements mr.DataReduce {
        @Override
        protected void reduce(Text key, Iterable<Text> value, Context context) throws java.io.IOException, InterruptedException {
            context.write(key, new Text(""));
        }
    }
}
