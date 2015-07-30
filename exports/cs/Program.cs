using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using cfg;

namespace test
{
    class Program
    {
        static void Main(string[] args)
        {
            CfgMgr.DataDir.Dir = "f:/workspace/luxian/luxian/cfggen/data";
            System.Console.WriteLine("++++");
            CfgMgr.Load();
        }
    }
}
