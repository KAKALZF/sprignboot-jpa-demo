package com.wisely.web;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import com.wisely.dao.PersonRepository;
import com.wisely.dao.TmallOrderDao;
import com.wisely.domain.Person;
import com.wisely.domain.TmallOrder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
public class DataController {
    //1 Spring Data JPA已自动为你注册bean，所以可自动注入
    @Autowired
    PersonRepository personRepository;
    @Autowired
    TmallOrderDao tmallOrderDao;

    /**
     * 保存
     * save支持批量保存：<S extends T> Iterable<S> save(Iterable<S> entities);
     * <p>
     * 删除：
     * 删除支持使用id，对象以，批量删除及删除全部：
     * void delete(ID id);
     * void delete(T entity);
     * void delete(Iterable<? extends T> entities);
     * void deleteAll();
     */
    @RequestMapping("/save")
    public Person save(String name, String address, Integer age) {

        Person p = personRepository.save(new Person(null, name, age, address));

        return p;

    }


    /**
     * 测试findByAddress
     */
    @RequestMapping("/q1")
    public List<Person> q1(String address) {

        List<Person> people = personRepository.findByAddress(address);

        return people;

    }

    /**
     * 测试findByNameAndAddress
     */
    @RequestMapping("/q2")
    public Person q2(String name, String address) {
        Person people = personRepository.findByNameAndAddress(name, address);
        return people;
    }

    /**
     * 测试withNameAndAddressQuery
     */
    @RequestMapping("/q3")
    public Person q3(String name, String address) {

        Person p = personRepository.withNameAndAddressQuery(name, address);

        return p;

    }

    /**
     * 测试withNameAndAddressNamedQuery
     */
    @RequestMapping("/q4")
    public Person q4(String name, String address) {

        Person p = personRepository.withNameAndAddressNamedQuery(name, address);

        return p;

    }

    /**
     * 测试排序
     */
    @RequestMapping("/sort")
    public List<Person> sort() {

        List<Person> people = personRepository.findAll(new Sort(Direction.ASC, "age"));

        return people;

    }

    /**
     * 测试分页
     */
    @RequestMapping("/page")
    public Page<Person> page() {

        Page<Person> pagePeople = personRepository.findAll(new PageRequest(1, 2));

        return pagePeople;

    }


    @RequestMapping("/auto")
    public Page<Person> auto(Person person) {

        Page<Person> pagePeople = personRepository.findByAuto(person, new PageRequest(0, 10));

        return pagePeople;

    }

    public static String filePath = "C:\\Users\\57580\\Desktop\\号码\\201802";

    @RequestMapping("/tmall")
    public String tmall() {
        try {
            readeCsv(filePath, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    public void readeCsv(String path, int ignoreRows) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(15);
        File dir = new File(path);
        File[] files = dir.listFiles();
        System.out.println("共有" + files.length + "个文件");
        List<TmallOrder> objects = new ArrayList<TmallOrder>();
        for (File file : files) {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName
                    ("GBK")));
            String line = "";
            bReader.readLine();//不要标题
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(",");
                List<String> strings = Arrays.asList(split);
                strings = strings.stream().filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toList());
                int size1 = strings.size();
                System.out.println("共有" + size1 + "列");
                if (size1 != 20) {
                    continue;
                }
                int size = strings.size();
                TmallOrder tmallOrder = new TmallOrder();
                tmallOrder.setOrderNum(strings.get(0));
                tmallOrder.setOriginNum(strings.get(1));
                tmallOrder.setOrderSource(strings.get(2));
                tmallOrder.setPhoneNum(strings.get(3));
                tmallOrder.setOperator(strings.get(4));
                tmallOrder.setAffiliation(strings.get(5));
                tmallOrder.setProduct(strings.get(6));
                tmallOrder.setSpu(strings.get(7));
                tmallOrder.setProductSize(strings.get(8));
                tmallOrder.setSup(strings.get(9));
                tmallOrder.setSalePrice(Double.valueOf(strings.get(10)));
                tmallOrder.setProfit(Double.valueOf(strings.get(11)));
                tmallOrder.setCard(Integer.parseInt(strings.get(12)));
                tmallOrder.setChannel(strings.get(13));
                tmallOrder.setResult(strings.get(14));
                tmallOrder.setWorkTime(strings.get(15));
                tmallOrder.setReturnCode(strings.get(16));
                tmallOrder.setCopeWith(strings.get(17));
                //tmallOrder.setCallback(strings.get(18));
                objects.add(tmallOrder);
                if (objects.size() == 250) {
                    pool.execute(new Runnable() {
                        @Override
                        public void run() {
                            TmallOrder[] tmalls = new TmallOrder[250];
                            for (int i = 0; i < 250; i++) {
                                tmalls[i] = objects.get(i);
                            }
                            // TmallOrder[] objects1 = (TmallOrder[]) objects.toArray();
                            System.out.println(Thread.currentThread());
                            tmallOrderDao.save(new ArrayIterator<TmallOrder>(tmalls));
                        }
                    });
                    objects.clear();
                }
            }
        }
    }

}
