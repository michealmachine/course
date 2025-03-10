'use client';

import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { usePermissionStore } from '@/stores/permission-store';
import { Permission } from '@/types/permission';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

// 权限表单验证Schema
const permissionFormSchema = z.object({
  name: z.string()
    .min(2, '权限名称至少需要2个字符')
    .max(50, '权限名称不能超过50个字符'),
  code: z.string()
    .min(4, '权限编码至少需要4个字符')
    .max(50, '权限编码不能超过50个字符')
    .regex(/^[A-Z0-9_]+$/, '权限编码只能包含大写字母、数字和下划线'),
  description: z.string().optional(),
  url: z.string().optional(),
  method: z.string().optional(),
});

// 表单值类型
type PermissionFormValues = z.infer<typeof permissionFormSchema>;

// HTTP方法选项
const httpMethods = [
  { value: 'GET', label: 'GET' },
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
  { value: 'DELETE', label: 'DELETE' },
  { value: 'PATCH', label: 'PATCH' },
];

export function PermissionForm() {
  // 获取权限状态
  const { 
    currentPermission, 
    formVisible, 
    isLoading,
    setFormVisible, 
    createPermission, 
    updatePermission 
  } = usePermissionStore();

  // 初始化表单
  const form = useForm<PermissionFormValues>({
    resolver: zodResolver(permissionFormSchema),
    defaultValues: {
      name: '',
      code: '',
      description: '',
      url: '',
      method: '',
    },
  });

  // 当currentPermission变化时，更新表单值
  useEffect(() => {
    if (currentPermission) {
      form.reset({
        name: currentPermission.name,
        code: currentPermission.code,
        description: currentPermission.description || '',
        url: currentPermission.url || '',
        method: currentPermission.method || '',
      });
    } else {
      form.reset({
        name: '',
        code: '',
        description: '',
        url: '',
        method: '',
      });
    }
  }, [currentPermission, form]);

  // 提交表单
  const onSubmit = async (values: PermissionFormValues) => {
    // 如果是编辑权限
    if (currentPermission) {
      await updatePermission(currentPermission.id, values);
    } else {
      // 如果是创建权限
      await createPermission(values);
    }
  };

  return (
    <Dialog open={formVisible} onOpenChange={setFormVisible}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>
            {currentPermission ? '编辑权限' : '创建权限'}
          </DialogTitle>
        </DialogHeader>
        
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            {/* 权限名称 */}
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>权限名称</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入权限名称" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            {/* 权限编码 */}
            <FormField
              control={form.control}
              name="code"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>权限编码</FormLabel>
                  <FormControl>
                    <Input 
                      placeholder="请输入权限编码（大写字母、数字和下划线）" 
                      {...field} 
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            {/* 权限描述 */}
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>权限描述</FormLabel>
                  <FormControl>
                    <Textarea 
                      placeholder="请输入权限描述" 
                      className="resize-none" 
                      {...field} 
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            {/* 资源URL */}
            <FormField
              control={form.control}
              name="url"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>资源URL</FormLabel>
                  <FormControl>
                    <Input 
                      placeholder="请输入资源URL（如：/api/users/{id}）" 
                      {...field} 
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            {/* HTTP方法 */}
            <FormField
              control={form.control}
              name="method"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>HTTP方法</FormLabel>
                  <Select 
                    onValueChange={field.onChange} 
                    value={field.value} 
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="请选择HTTP方法" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {httpMethods.map(method => (
                        <SelectItem key={method.value} value={method.value}>
                          {method.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            <DialogFooter className="mt-6">
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => setFormVisible(false)}
                disabled={isLoading}
              >
                取消
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? '提交中...' : '提交'}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
} 