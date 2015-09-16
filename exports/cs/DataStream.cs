using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace cfg
{
    public class DataStream
    {


        private readonly string[] line;
        private int index;
        DataStream(string file, string encoding)
        {
            line = File.ReadAllLines(file);
            index = 0;
        }

        public string GetNext()
        {
            return index < line.Length ? line[index++] : null;
        }

        void Error(string err)
        {
            throw new Exception(err);
        }

        string GetNextAndCheckNotEmpty()
        {
            var s = GetNext();
            if (s == null)
                Error("read not enough");
            return s;
        }

        public string GetString()
        {
            return GetNextAndCheckNotEmpty();
        }

        public float GetFloat()
        {
            return float.Parse(GetNextAndCheckNotEmpty());
        }


        public int GetInt()
        {
            return int.Parse(GetNextAndCheckNotEmpty());
        }

        public bool GetBool()
        {
            var s = GetNextAndCheckNotEmpty().ToLower();
            if (s == "true")
                return true;
            if (s == "false")
                return false;
            Error(s + " isn't bool");
            return false;
        }

        public static DataStream Create(string file, string encoding)
        {
            return new DataStream(file, encoding);
        }
    }
}
