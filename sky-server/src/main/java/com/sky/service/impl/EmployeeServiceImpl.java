package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传来的密码进行MD5加密处理
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工账号
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO)  {
        Employee employee=new Employee();

        //将employeeDTO的信息复制给employee
        BeanUtils.copyProperties(employeeDTO,employee);

        //员工账号访问状态，1为可以访问，0为禁止访问
        employee.setStatus(StatusConstant.ENABLE);

        //设置员工账号密码，初始密码为：123456，并加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //员工账号创建和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //员工账号创建者与修改者的id
        Long  empId = BaseContext.getCurrentId();
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询员工表单
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult queryByPage(EmployeePageQueryDTO employeePageQueryDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());

      Page<Employee>pages = employeeMapper.pageQuery(employeePageQueryDTO);

      return new PageResult(pages.getTotal(),pages.getResult());
    }

    /**
     * 启用或者禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void enableOrDisable(Integer status, Long id) {
        Employee employee= Employee.builder()
                .id(id)
                .status(status)
                .build();
        employeeMapper.updateEmployee(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee idQuery(Long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setPassword("*****");//将返回给前端的信息加密
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void updateEmployeeInfo(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();

        //将employeeDTO的信息复制给employee
        BeanUtils.copyProperties(employeeDTO,employee);

        //员工账号修改时间
        employee.setUpdateTime(LocalDateTime.now());

        //员工账号修改者的id
        Long  empId = BaseContext.getCurrentId();
        employee.setUpdateUser(empId);

        employeeMapper.updateEmployee(employee);
    }
}
