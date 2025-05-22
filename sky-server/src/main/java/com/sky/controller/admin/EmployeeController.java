package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "管理员相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation(value = "员工登录")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @ApiOperation(value = "新增员工")
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }


    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @ApiOperation(value = "分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("分页查询参数：员工姓名={},page={},pageSize={}",employeePageQueryDTO.getName(),employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用、禁用员工账号
     * @param id
     * @param status
     * @return
     */
    @ApiOperation(value = "启用、禁用员工账号")
    @PostMapping("/status/{status}")
    public Result<PageResult> startOrStop(@RequestParam(value = "id") Long id,
                                          @PathVariable(value = "status") Integer status){
        log.info("启用、禁用员工账号：启用=1 | 禁用=0 ：{},员工id：{}",status,id);
        employeeService.startOrStop(id,status);
        return Result.success();
    }


    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询员工")
    @GetMapping("/{id}")
    public Result getById(@PathVariable(value = "id") Long id){
        log.info("查询员工的id：{}",id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }


    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @ApiOperation("编辑员工信息")
    @PutMapping()
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工的信息：{}",employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

}
