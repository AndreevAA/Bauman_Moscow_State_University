#include "common.h"

#include "32_bit_measures.h"

void get_32_bit_sum(float a, float b, size_t count)
{
    float c;
    for (size_t i = 0; i < count; ++i)
        c = a + b;
}

#ifdef ASM
void get_32_bit_sum_asm(float a, float b, size_t count)
{
    float c;

    for (size_t i = 0; i < count; ++i)
        asm("fld %1\n"
            "fld %2\n"
            "faddp\n"
            "fstp %0\n"
            : "=m"(c)
            : "m"(a), "m"(b));
}
#endif

// 
#ifdef ASM
void get_32_bit_sum_asm_proc(float a, float b, size_t count)
{
    float c;

    for (size_t i = 0; i < count; ++i)
    {
        asm(
            "mov eax, %1\n"
            "add eax, %2\n"
            "mov %0, eax\n" : "=m"(c)
            : "m"(a), "m"(b)
            );
    }

    printf("Сложение процессором: %lf + %lf = %lf\n", a, b, c);
}
#endif

void get_32_bit_prod(float a, float b, size_t count)
{
    float c;
    for (size_t i = 0; i < count; ++i)
        c = a * b;
}

#ifdef ASM
void get_32_bit_prod_asm(float a, float b, size_t count)
{
    float c;

    for (size_t i = 0; i < count; ++i)
        asm("fld %1\n"
            "fld %2\n"
            "fmulp\n"
            "fstp %0\n"
            : "=m"(c)
            : "m"(a), "m"(b));
}
#endif

void out_32_bit_dimension()
{
    printf("float type size: %zu bites\n", sizeof(float) * CHAR_BIT);

    float a = 1000.9797, b = 100000.456789;

    clock_t begin = clock();
    get_32_bit_sum(a, b, repeats);
    clock_t end = clock();

    printf(SUM_FMT, (double)(end - begin) / CLOCKS_PER_SEC / repeats);

    // Вычисление на процессоре
    #ifdef ASM
    begin = clock();
    get_32_bit_sum_asm_proc(a, b, repeats);
    end = clock();

    printf(ASM_SUM_FMT, (double)(end - begin) / CLOCKS_PER_SEC / repeats);
    #endif

    // #ifdef ASM
    // begin = clock();
    // get_32_bit_sum_asm(a, b, repeats);
    // end = clock();

    // printf(ASM_SUM_FMT, (double)(end - begin) / CLOCKS_PER_SEC / repeats);
    // #endif

    begin = clock();
    get_32_bit_prod(a, b, repeats);
    end = clock();

    printf(PROD_FMT, (double)(end - begin) / CLOCKS_PER_SEC / repeats);

    #ifdef ASM
    begin = clock();
    get_32_bit_prod_asm(a, b, repeats);
    end = clock();

    printf(ASM_PROD_FMT, (double)(end - begin) / CLOCKS_PER_SEC / repeats);
    #endif

    printf("\n");
}
