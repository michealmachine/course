import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export default function HomePage() {
  return (
    <div className="min-h-screen flex flex-col">
      {/* 导航栏 */}
      <header className="border-b bg-background">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="mr-2 text-primary"
            >
              <path d="M22 2 11 13"></path>
              <path d="m22 2-7 20-4-9-9-4 20-7z"></path>
            </svg>
            <span className="text-xl font-semibold">在线课程平台</span>
          </div>
          <div className="flex gap-4 items-center">
            <nav className="hidden md:flex mr-6">
              <ul className="flex space-x-6">
                <li>
                  <Link href="/courses" className="text-muted-foreground hover:text-primary">
                    课程
                  </Link>
                </li>
                <li>
                  <Link href="/institution" className="text-muted-foreground hover:text-primary">
                    机构中心
                  </Link>
                </li>
                <li>
                  <Link href="/about" className="text-muted-foreground hover:text-primary">
                    关于我们
                  </Link>
                </li>
              </ul>
            </nav>
            <Button variant="outline" asChild>
              <Link href="/login">登录</Link>
            </Button>
            <Button asChild>
              <Link href="/register">注册</Link>
            </Button>
          </div>
        </div>
      </header>

      {/* 英雄区域 */}
      <section className="bg-primary text-primary-foreground py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-6">提升技能，开启未来</h1>
          <p className="text-xl mb-8 max-w-2xl mx-auto">
            我们的在线课程平台提供高质量的学习内容，帮助您掌握最前沿的技能和知识
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" asChild>
              <Link href="/courses">浏览课程</Link>
            </Button>
            <Button size="lg" variant="outline" className="bg-primary/10 text-primary-foreground hover:bg-primary/20" asChild>
              <Link href="/institution">机构入驻</Link>
            </Button>
          </div>
        </div>
      </section>

      {/* 特色区域 */}
      <section className="py-16">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">为什么选择我们</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                title: '高质量内容',
                description: '由行业专家精心打造的课程内容，确保学习效果',
                icon: (
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                    <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z" />
                  </svg>
                ),
              },
              {
                title: '灵活学习',
                description: '随时随地学习，按照自己的节奏掌握新技能',
                icon: (
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                    <path d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z" />
                    <path d="M10 2v2" />
                    <path d="M18 12h2" />
                    <path d="M12 18v2" />
                    <path d="M4 12H2" />
                    <path d="M12 6v6l4 2" />
                  </svg>
                ),
              },
              {
                title: '专业支持',
                description: '遇到问题随时可获得讲师和社区的专业支持',
                icon: (
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                    <path d="M21 12a9 9 0 0 1-9 9 9 9 0 0 1-9-9 9 9 0 0 1 9-9 9 9 0 0 1 9 9z" />
                    <path d="M12 16v-3" />
                    <path d="M12 8h.01" />
                  </svg>
                ),
              },
            ].map((feature, i) => (
              <Card key={i}>
                <CardContent className="text-center p-6">
                  <div className="h-12 w-12 mx-auto mb-4 flex items-center justify-center rounded-full bg-primary/10">
                    {feature.icon}
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{feature.title}</h3>
                  <p className="text-muted-foreground">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* 热门课程 */}
      <section className="py-16 bg-muted/30">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">热门课程</h2>
          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                title: 'Web前端开发',
                description: '掌握HTML、CSS、JavaScript和现代前端框架',
                image: 'https://source.unsplash.com/random/300x200?web',
              },
              {
                title: '数据科学入门',
                description: '学习数据分析、可视化和机器学习基础',
                image: 'https://source.unsplash.com/random/300x200?data',
              },
              {
                title: '移动应用开发',
                description: '创建跨平台移动应用的技能和最佳实践',
                image: 'https://source.unsplash.com/random/300x200?mobile',
              },
            ].map((course, i) => (
              <Card key={i} className="overflow-hidden">
                <div className="h-48 bg-muted relative">
                  <div className="absolute inset-0 flex items-center justify-center bg-primary/10">
                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" className="text-muted-foreground">
                      <rect width="18" height="18" x="3" y="3" rx="2" />
                      <path d="M7 3v18" />
                      <path d="M3 7h18" />
                    </svg>
                  </div>
                </div>
                <CardContent className="p-6">
                  <h3 className="text-xl font-semibold mb-2">{course.title}</h3>
                  <p className="text-muted-foreground mb-4">{course.description}</p>
                  <Button variant="outline" className="w-full" asChild>
                    <Link href="/courses">查看详情</Link>
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
          <div className="text-center mt-10">
            <Button variant="outline" size="lg" asChild>
              <Link href="/courses">查看全部课程</Link>
            </Button>
          </div>
        </div>
      </section>

      {/* 底部 */}
      <footer className="py-12 bg-muted">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center mb-6 md:mb-0">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="mr-2 text-primary"
              >
                <path d="M22 2 11 13"></path>
                <path d="m22 2-7 20-4-9-9-4 20-7z"></path>
              </svg>
              <span className="text-xl font-semibold">在线课程平台</span>
            </div>
            <div className="flex gap-6">
              <Link href="/about" className="hover:text-primary">关于我们</Link>
              <Link href="/contact" className="hover:text-primary">联系我们</Link>
              <Link href="/privacy" className="hover:text-primary">隐私政策</Link>
              <Link href="/terms" className="hover:text-primary">服务条款</Link>
            </div>
          </div>
          <div className="mt-8 text-center text-muted-foreground">
            <p>© {new Date().getFullYear()} 在线课程平台. 保留所有权利.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
