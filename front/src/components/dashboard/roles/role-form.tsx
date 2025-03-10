'use client';

import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';

import { useRoleStore } from '@/stores/role-store';
import { Role } from '@/types/role';

// 表单验证模式
const formSchema = z.object({
  name: z.string()
    .min(2, { message: '角色名称至少需要2个字符' })
    .max(50, { message: '角色名称不能超过50个字符' }),
  code: z.string()
    .min(4, { message: '角色编码至少需要4个字符' })
    .max(50, { message: '角色编码不能超过50个字符' })
    .regex(/^ROLE_[A-Z0-9_]+$/, { 
      message: '角色编码必须以ROLE_开头，且只能包含大写字母、数字和下划线' 
    }),
  description: z.string().max(200, { message: '描述不能超过200个字符' }).optional(),
});

// 表单值类型
type FormValues = z.infer<typeof formSchema>;

export function RoleForm() {
  // 从store获取状态和方法
  const { 
    formVisible, 
    setFormVisible, 
    currentRole, 
    isLoading, 
    createRole, 
    updateRole 
  } = useRoleStore();

  // 创建表单
  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: '',
      code: 'ROLE_',
      description: '',
    },
  });

  // 当currentRole变化时更新表单值
  useEffect(() => {
    if (currentRole) {
      form.reset({
        name: currentRole.name,
        code: currentRole.code,
        description: currentRole.description || '',
      });
    } else {
      form.reset({
        name: '',
        code: 'ROLE_',
        description: '',
      });
    }
  }, [currentRole, form]);

  // 提交表单
  const onSubmit = async (values: FormValues) => {
    if (currentRole) {
      // 更新角色
      await updateRole(currentRole.id, values);
    } else {
      // 创建角色
      await createRole(values);
    }
  };

  return (
    <Dialog open={formVisible} onOpenChange={setFormVisible}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{currentRole ? '编辑角色' : '创建角色'}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>角色名称</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入角色名称" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="code"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>角色编码</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入角色编码 (格式: ROLE_XXX)" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>描述</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="请输入角色描述"
                      className="resize-none"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="flex justify-end space-x-2 pt-4">
              <Button variant="outline" onClick={() => setFormVisible(false)} disabled={isLoading}>取消</Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {currentRole ? '更新' : '创建'}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
} 