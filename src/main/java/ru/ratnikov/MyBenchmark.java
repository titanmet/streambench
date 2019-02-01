package ru.ratnikov;

import org.openjdk.jmh.annotations.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MyBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState{
        public static final int SEED=42;
        public static final int ARRAY_LENGTH=1_000_000;

        volatile Person[] array;

        @Setup
        public void initState(){
        Random random=new Random(SEED);
        this.array=new Person[ARRAY_LENGTH];
            for (int i = 0; i < this.array.length; i++) {
                Person person=new Person();
                person.setName("N"+random.nextInt());
                person.setAge(random.nextInt(200));
                person.setChildren(random.nextInt(100));
                this.array[i]=person;
            }
        }

        @Benchmark
        public List<Person> streamApi(BenchmarkState state){
            List<Person> result= Arrays
                    .stream(state.array)
                    .filter(p->p.getAge()>18)
                    .filter(p->p.getChildren()>50)
                    .collect(Collectors.toList());
            return result;
        }

        @Benchmark
        public List<Person> streamApiFilter(BenchmarkState state){
            List<Person> result= Arrays
                    .stream(state.array)
                    .filter(p->p.getAge()>18 && p.getChildren()>50)
                    .collect(Collectors.toList());
            return result;
        }

        @Benchmark
        public List<Person> parallelStreamApi(BenchmarkState state){
            List<Person> result= Arrays
                    .stream(state.array)
                    .parallel()
                    .filter(p->p.getAge()>18)
                    .filter(p->p.getChildren()>50)
                    .collect(Collectors.toList());
            return result;
        }

        @Benchmark
        public int parallelStreamApiFilter(BenchmarkState state){
            int result= Arrays
                    .stream(state.array)
                    .parallel()
                    .filter(p->p.getAge()>18 && p.getChildren()>50)
                    .mapToInt(Person::getAge)
                    .reduce(0,(x,y)->x+y);
            return result;
        }

        @Benchmark
        public List<Person> guava(BenchmarkState state){
            List<Person> list=Arrays.asList(state.array);
            Iterable result = Iterables.filter(list, new Predicate<Person>() {
                @Override
                public boolean apply(@Nullable Person person) {
                    return person.getAge()>18;
                }
            });
            result=Iterables.filter(result, new Predicate<Person>() {
                @Override
                public boolean apply(@Nullable Person person) {
                    return person.getChildren()>50;
                }
            });
            return Lists.newArrayList(result);
        }


    }
}
