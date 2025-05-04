'use client';

import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { useUserManagementStore } from '@/stores/user-management-store';
import { useRoleStore } from '@/stores/role-store';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { UserDTO } from '@/types/user';

// 表单验证模式
const formSchema = z.object({
  username: z.string().min(3, '用户名至少需要3个字符').max(50, '用户名最多50个字符'),
  password: z.string().min(6, '密码至少需要6个字符').max(100, '密码最多100个字符').optional(),
  email: z.string().email('请输入有效的邮箱地址'),
  phone: z.string().regex(/^1[3-9]\d{9}$/, '请输入有效的手机号码').optional(),
  nickname: z.string().min(2, '昵称至少需要2个字符').max(50, '昵称最多50个字符'),
  status: z.number(),
  roleIds: z.array(z.number()).min(1, '请至少选择一个角色'),
});

export function UserForm() {
  const {
    formVisible,
    setFormVisible,
    currentUser,
    createUser,
    updateUser,
    isLoading
  } = useUserManagementStore();

  const { roles, fetchRoles } = useRoleStore();

  // 初始化表单
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      username: '',
      password: '',
      email: '',
      phone: '',
      nickname: '',
      status: 1, // 默认启用
      roleIds: [],
    },
  });

  // 加载角色列表
  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  // 当currentUser变化时，更新表单值
  useEffect(() => {
    console.log('用户表单 - 当前用户:', currentUser);
    console.log('用户表单 - 当前用户角色:', currentUser?.roles);

    if (currentUser) {
      // 确保角色ID是数组，即使后端没有返回角色信息
      let roleIds: number[] = [];

      if (currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.length > 0) {
        roleIds = currentUser.roles.map(role => role.id);
      }

      console.log('用户表单 - 提取的角色IDs:', roleIds);

      form.reset({
        username: currentUser.username,
        // 编辑时不设置密码
        password: undefined,
        email: currentUser.email,
        phone: currentUser.phone || '',
        nickname: currentUser.nickname || '',
        status: currentUser.status || 1,
        roleIds: roleIds,
      });
    } else {
      form.reset({
        username: '',
        password: '',
        email: '',
        phone: '',
        nickname: '',
        status: 1,
        roleIds: [],
      });
    }
  }, [currentUser, form]);

  // 表单提交处理
  const onSubmit = async (values: z.infer<typeof formSchema>) => {
    const userData: UserDTO = {
      ...values,
      // 如果是编辑模式且密码为空，则不传递密码字段
      ...(currentUser && !values.password && { password: undefined }),
    };

    if (currentUser) {
      await updateUser(currentUser.id, userData);
    } else {
      await createUser(userData);
    }
  };

  return (
    <Dialog open={formVisible} onOpenChange={setFormVisible}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{currentUser ? '编辑用户' : '创建用户'}</DialogTitle>
          <DialogDescription>
            {currentUser
              ? '修改用户信息，如不修改密码请留空'
              : '填写用户信息，创建新用户'}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>用户名</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="请输入用户名"
                      {...field}
                      disabled={!!currentUser} // 编辑模式下用户名不可修改
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{currentUser ? '密码 (留空则不修改)' : '密码'}</FormLabel>
                  <FormControl>
                    <Input
                      type="password"
                      placeholder={currentUser ? "留空则不修改密码" : "请输入密码"}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>邮箱</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入邮箱" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="phone"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>手机号</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入手机号" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="nickname"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>昵称</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入昵称" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="status"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>状态</FormLabel>
                  <Select
                    onValueChange={(value) => field.onChange(parseInt(value))}
                    defaultValue={field.value.toString()}
                    value={field.value.toString()}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="选择用户状态" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="1">启用</SelectItem>
                      <SelectItem value="0">禁用</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="roleIds"
              render={() => (
                <FormItem>
                  <div className="mb-2">
                    <FormLabel>角色</FormLabel>
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    {roles.map((role) => (
                      <FormField
                        key={role.id}
                        control={form.control}
                        name="roleIds"
                        render={({ field }) => {
                          return (
                            <FormItem
                              key={role.id}
                              className="flex flex-row items-start space-x-3 space-y-0"
                            >
                              <FormControl>
                                <Checkbox
                                  checked={field.value?.includes(role.id)}
                                  onCheckedChange={(checked) => {
                                    const currentValues = field.value || [];
                                    if (checked) {
                                      field.onChange([...currentValues, role.id]);
                                    } else {
                                      field.onChange(
                                        currentValues.filter((value) => value !== role.id)
                                      );
                                    }
                                  }}
                                />
                              </FormControl>
                              <FormLabel className="font-normal cursor-pointer">
                                {role.name}
                              </FormLabel>
                            </FormItem>
                          );
                        }}
                      />
                    ))}
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setFormVisible(false)}
                disabled={isLoading}
              >
                取消
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? '处理中...' : (currentUser ? '更新' : '创建')}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}