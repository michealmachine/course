'use client';

import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import {
  Check as CheckIcon,
  XCircle,
  ChevronDown,
  X as XIcon,
  WandSparkles,
  ChevronsUpDown,
} from "lucide-react";
import { useEffect, useState } from "react";

import { cn } from "@/lib/utils";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from "@/components/ui/command";

/**
 * Variants for the multi-select component to handle different styles.
 */
const multiSelectVariants = cva(
  "m-1 transition ease-in-out delay-150 hover:-translate-y-1 hover:scale-110 duration-300",
  {
    variants: {
      variant: {
        default:
          "border-foreground/10 text-foreground bg-card hover:bg-card/80",
        secondary:
          "border-foreground/10 bg-secondary text-secondary-foreground hover:bg-secondary/80",
        destructive:
          "border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80",
        inverted: "inverted",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

/**
 * 导出OptionType类型以便重用
 */
export type OptionType = {
  label: string;
  value: number | string;
  icon?: React.ComponentType<{ className?: string }>;
};

/**
 * Props for MultiSelect component
 */
interface MultiSelectProps
  extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, 'onChange' | 'defaultValue'>,
    VariantProps<typeof multiSelectVariants> {
  /**
   * 选项数组
   */
  options: OptionType[];

  /**
   * 选中值变化时的回调函数 (新的接口兼容原始组件)
   */
  onValueChange?: (value: (number | string)[]) => void;
  
  /**
   * 当前选中的值 (新的接口兼容原始组件)
   */
  defaultSelectedValues?: (number | string)[];
  
  /**
   * 当前选中的值 (保留旧接口以兼容现有代码)
   */
  selected?: (number | string)[];
  
  /**
   * 值变化时的回调 (保留旧接口以兼容现有代码)
   */
  onSelectChange?: (selected: (number | string)[]) => void;

  /**
   * 占位符文本
   */
  placeholder?: string;

  /**
   * 动画持续时间（秒）
   */
  animation?: number;

  /**
   * 最多显示的选中项数量
   */
  maxCount?: number;

  /**
   * 是否为模态弹出框
   */
  modalPopover?: boolean;

  /**
   * 禁用状态
   */
  disabled?: boolean;
}

export const MultiSelect = React.forwardRef<
  HTMLButtonElement,
  MultiSelectProps
>(
  (
    {
      options,
      onValueChange,
      onSelectChange,
      variant,
      selected = [],
      defaultSelectedValues,
      placeholder = "选择选项",
      animation = 0,
      maxCount = 3,
      modalPopover = false,
      className,
      disabled = false,
      ...props
    },
    ref
  ) => {
    // 使用 defaultSelectedValues 或 selected 作为初始值
    const initialValues = defaultSelectedValues || selected || [];
    const [selectedValues, setSelectedValues] = 
      React.useState<(number | string)[]>(initialValues);
    const [isPopoverOpen, setIsPopoverOpen] = React.useState(false);
    const [isAnimating, setIsAnimating] = React.useState(false);
    const [searchTerm, setSearchTerm] = React.useState("");
    const [filteredOptions, setFilteredOptions] = React.useState(options);
    
    // 当外部 selected 或 defaultSelectedValues 值变化时更新内部状态
    React.useEffect(() => {
      if (defaultSelectedValues !== undefined) {
        setSelectedValues(defaultSelectedValues);
      } else if (selected !== undefined) {
        setSelectedValues(selected);
      }
    }, [defaultSelectedValues, selected]);

    // 当搜索词改变时过滤选项
    React.useEffect(() => {
      if (searchTerm) {
        setFilteredOptions(
          options.filter((option) => 
            option.label.toLowerCase().includes(searchTerm.toLowerCase())
          )
        );
      } else {
        setFilteredOptions(options);
      }
    }, [searchTerm, options]);

    const handleInputKeyDown = (
      event: React.KeyboardEvent<HTMLInputElement>
    ) => {
      if (event.key === "Enter") {
        setIsPopoverOpen(true);
      } else if (event.key === "Backspace" && !event.currentTarget.value) {
        const newSelectedValues = [...selectedValues];
        newSelectedValues.pop();
        setSelectedValues(newSelectedValues);
        // 调用两个回调函数，确保兼容性
        if (onSelectChange) onSelectChange(newSelectedValues);
        if (onValueChange) onValueChange(newSelectedValues);
      }
    };

    const toggleOption = (option: number | string) => {
      if (disabled) return;
      
      const newSelectedValues = selectedValues.includes(option)
        ? selectedValues.filter((value) => value !== option)
        : [...selectedValues, option];
      
      setSelectedValues(newSelectedValues);
      
      // 调用两个回调函数，确保兼容性
      if (onSelectChange) onSelectChange(newSelectedValues);
      if (onValueChange) onValueChange(newSelectedValues);
      
      // 保持弹出框打开，以便连续选择
      setIsPopoverOpen(true);
      
      // 添加额外的动画效果和反馈
      setIsAnimating(true);
      setTimeout(() => setIsAnimating(false), 300);
    };

    const handleClear = () => {
      if (disabled) return;
      
      setSelectedValues([]);
      
      // 调用两个回调函数，确保兼容性
      if (onSelectChange) onSelectChange([]);
      if (onValueChange) onValueChange([]);
    };

    const handleTogglePopover = () => {
      if (!disabled) {
        setIsPopoverOpen((prev) => !prev);
      }
    };

    const clearExtraOptions = () => {
      if (disabled) return;
      
      const newSelectedValues = selectedValues.slice(0, maxCount);
      setSelectedValues(newSelectedValues);
      
      // 调用两个回调函数，确保兼容性
      if (onSelectChange) onSelectChange(newSelectedValues);
      if (onValueChange) onValueChange(newSelectedValues);
    };

    const toggleAll = () => {
      if (disabled) return;
      
      if (selectedValues.length === options.length) {
        handleClear();
      } else {
        const allValues = options.map((option) => option.value);
        setSelectedValues(allValues);
        
        // 调用两个回调函数，确保兼容性
        if (onSelectChange) onSelectChange(allValues);
        if (onValueChange) onValueChange(allValues);
      }
    };

    return (
      <Popover
        open={isPopoverOpen}
        onOpenChange={setIsPopoverOpen}
        modal={modalPopover}
      >
        <PopoverTrigger asChild>
          <Button
            ref={ref}
            type="button"
            {...props}
            disabled={disabled}
            onClick={handleTogglePopover}
            className={cn(
              "flex w-full p-1 rounded-md border min-h-10 h-auto items-center justify-between bg-background hover:bg-background/90 hover:border-primary focus:border-primary [&_svg]:pointer-events-auto",
              disabled && "opacity-50 cursor-not-allowed",
              !disabled && "cursor-pointer",
              className
            )}
            aria-expanded={isPopoverOpen}
            aria-haspopup="true"
            aria-label={`选择${placeholder}`}
            data-trigger="true"
          >
            {selectedValues.length > 0 ? (
              <div className="flex justify-between items-center w-full">
                <div className="flex flex-wrap items-center">
                  {selectedValues.slice(0, maxCount).map((value) => {
                    const option = options.find((o) => o.value === value);
                    const IconComponent = option?.icon;
                    return (
                      <Badge
                        key={String(value)}
                        className={cn(
                          isAnimating ? "animate-bounce" : "",
                          multiSelectVariants({ variant })
                        )}
                        style={{ animationDuration: `${animation}s` }}
                      >
                        {IconComponent && (
                          <IconComponent className="h-4 w-4 mr-2" />
                        )}
                        {option?.label}
                        {!disabled && (
                          <XCircle
                            className="ml-2 h-4 w-4 cursor-pointer"
                            onClick={(event) => {
                              event.stopPropagation();
                              toggleOption(value);
                            }}
                          />
                        )}
                      </Badge>
                    );
                  })}
                  {selectedValues.length > maxCount && (
                    <Badge
                      className={cn(
                        "bg-transparent text-foreground border-foreground/10 hover:bg-transparent",
                        isAnimating ? "animate-bounce" : "",
                        multiSelectVariants({ variant })
                      )}
                      style={{ animationDuration: `${animation}s` }}
                    >
                      {`+ ${selectedValues.length - maxCount} 项`}
                      {!disabled && (
                        <XCircle
                          className="ml-2 h-4 w-4 cursor-pointer"
                          onClick={(event) => {
                            event.stopPropagation();
                            clearExtraOptions();
                          }}
                        />
                      )}
                    </Badge>
                  )}
                </div>
                {!disabled && (
                  <div className="flex items-center justify-between">
                    <XIcon
                      className="h-4 mx-2 cursor-pointer text-muted-foreground"
                      onClick={(event) => {
                        event.stopPropagation();
                        handleClear();
                      }}
                    />
                    <Separator
                      orientation="vertical"
                      className="flex min-h-6 h-full"
                    />
                    <ChevronDown className="h-4 mx-2 cursor-pointer text-muted-foreground" />
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center justify-between w-full mx-auto">
                <span className="text-sm text-muted-foreground mx-3">
                  {placeholder}
                </span>
                <ChevronDown className="h-4 cursor-pointer text-muted-foreground mx-2" />
              </div>
            )}
          </Button>
        </PopoverTrigger>
        <PopoverContent
          className="w-auto p-0 z-50"
          align="start"
          onEscapeKeyDown={() => setIsPopoverOpen(false)}
          sideOffset={5}
          onInteractOutside={(e) => {
            // 只有当点击的不是触发器时才关闭
            if (!(e.target as HTMLElement).closest('[data-trigger="true"]')) {
              setIsPopoverOpen(false);
            }
          }}
        >
          <Command className="w-full">
            <CommandInput
              placeholder="搜索标签..."
              value={searchTerm}
              onValueChange={setSearchTerm}
              className="h-9"
            />
            <CommandList className="max-h-[300px] overflow-auto">
              <CommandEmpty>未找到匹配项</CommandEmpty>
              <CommandGroup>
                <CommandItem
                  key="all"
                  onSelect={toggleAll}
                  className="cursor-pointer"
                >
                  <div
                    className={cn(
                      "mr-2 flex h-4 w-4 items-center justify-center rounded-sm border border-primary",
                      selectedValues.length === options.length
                        ? "bg-primary text-primary-foreground"
                        : "opacity-50 [&_svg]:invisible"
                    )}
                  >
                    <CheckIcon className="h-4 w-4" />
                  </div>
                  <span>(全选)</span>
                </CommandItem>
                {filteredOptions.map((option) => {
                  const isSelected = selectedValues.includes(option.value);
                  return (
                    <CommandItem
                      key={String(option.value)}
                      onSelect={() => toggleOption(option.value)}
                      className="cursor-pointer"
                    >
                      <div
                        className={cn(
                          "mr-2 flex h-4 w-4 items-center justify-center rounded-sm border border-primary",
                          isSelected
                            ? "bg-primary text-primary-foreground"
                            : "opacity-50 [&_svg]:invisible"
                        )}
                      >
                        <CheckIcon className="h-4 w-4" />
                      </div>
                      {option.icon && (
                        <option.icon className="mr-2 h-4 w-4 text-muted-foreground" />
                      )}
                      <span>{option.label}</span>
                    </CommandItem>
                  );
                })}
              </CommandGroup>
              <CommandSeparator />
              <CommandGroup>
                <div className="flex items-center justify-between">
                  {selectedValues.length > 0 && (
                    <>
                      <CommandItem
                        onSelect={handleClear}
                        className="flex-1 justify-center cursor-pointer"
                      >
                        清除
                      </CommandItem>
                      <Separator
                        orientation="vertical"
                        className="flex min-h-6 h-full"
                      />
                    </>
                  )}
                  <CommandItem
                    onSelect={() => setIsPopoverOpen(false)}
                    className="flex-1 justify-center cursor-pointer max-w-full"
                  >
                    关闭
                  </CommandItem>
                </div>
              </CommandGroup>
            </CommandList>
          </Command>
        </PopoverContent>
        {animation > 0 && selectedValues.length > 0 && (
          <WandSparkles
            className={cn(
              "cursor-pointer my-2 text-foreground bg-background w-3 h-3",
              isAnimating ? "" : "text-muted-foreground"
            )}
            onClick={() => setIsAnimating(!isAnimating)}
          />
        )}
      </Popover>
    );
  }
);

MultiSelect.displayName = "MultiSelect"; 